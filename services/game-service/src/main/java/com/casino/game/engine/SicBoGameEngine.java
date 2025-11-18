package com.casino.game.engine;

import com.casino.game.dto.SicBoResultData;
import com.casino.game.service.RngService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Sic Bo Game Engine
 * Traditional Chinese dice game using three dice
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SicBoGameEngine {

    private final RngService rngService;

    private static final double HOUSE_EDGE = 0.028; // 2.8% house edge (average)

    public enum BetType {
        // Total bets
        SMALL(1.96),           // Total 4-10 (pays 1:1)
        BIG(1.96),             // Total 11-17 (pays 1:1)

        // Specific total bets
        TOTAL_4(61.11),        // Total = 4 (pays 60:1)
        TOTAL_5(31.94),        // Total = 5 (pays 30:1)
        TOTAL_6(18.98),        // Total = 6 (pays 17:1)
        TOTAL_7(13.19),        // Total = 7 (pays 12:1)
        TOTAL_8(9.03),         // Total = 8 (pays 8:1)
        TOTAL_9(7.41),         // Total = 9 (pays 6:1)
        TOTAL_10(7.41),        // Total = 10 (pays 6:1)
        TOTAL_11(7.41),        // Total = 11 (pays 6:1)
        TOTAL_12(7.41),        // Total = 12 (pays 6:1)
        TOTAL_13(9.03),        // Total = 13 (pays 8:1)
        TOTAL_14(13.19),       // Total = 14 (pays 12:1)
        TOTAL_15(18.98),       // Total = 15 (pays 17:1)
        TOTAL_16(31.94),       // Total = 16 (pays 30:1)
        TOTAL_17(61.11),       // Total = 17 (pays 60:1)

        // Single number bets (at least one die shows this number)
        SINGLE_1(1.0),         // One 1 (pays 1:1, two pays 2:1, three pays 3:1)
        SINGLE_2(1.0),
        SINGLE_3(1.0),
        SINGLE_4(1.0),
        SINGLE_5(1.0),
        SINGLE_6(1.0),

        // Double bets (at least two dice show specific number)
        DOUBLE_1(11.11),       // Pays 10:1
        DOUBLE_2(11.11),
        DOUBLE_3(11.11),
        DOUBLE_4(11.11),
        DOUBLE_5(11.11),
        DOUBLE_6(11.11),

        // Triple bets
        ANY_TRIPLE(31.94),     // Any three of a kind (pays 30:1)
        TRIPLE_1(181.0),       // Specific triple (pays 180:1)
        TRIPLE_2(181.0),
        TRIPLE_3(181.0),
        TRIPLE_4(181.0),
        TRIPLE_5(181.0),
        TRIPLE_6(181.0),

        // Two dice combination
        COMBO_1_2(7.14),       // Pays 6:1
        COMBO_1_3(7.14),
        COMBO_1_4(7.14),
        COMBO_1_5(7.14),
        COMBO_1_6(7.14),
        COMBO_2_3(7.14),
        COMBO_2_4(7.14),
        COMBO_2_5(7.14),
        COMBO_2_6(7.14),
        COMBO_3_4(7.14),
        COMBO_3_5(7.14),
        COMBO_3_6(7.14),
        COMBO_4_5(7.14),
        COMBO_4_6(7.14),
        COMBO_5_6(7.14);

        final double payoutMultiplier;

        BetType(double payoutMultiplier) {
            this.payoutMultiplier = payoutMultiplier;
        }
    }

    /**
     * Play a round of Sic Bo
     */
    public SicBoResultData play(String serverSeed, String clientSeed, long nonce,
                                 Map<BetType, BigDecimal> bets) {

        if (bets == null || bets.isEmpty()) {
            throw new IllegalArgumentException("At least one bet must be placed");
        }

        // Roll three dice
        int die1 = rngService.generateRandomNumber(serverSeed, clientSeed, nonce, 6) + 1;
        int die2 = rngService.generateRandomNumber(serverSeed, clientSeed, nonce + 1, 6) + 1;
        int die3 = rngService.generateRandomNumber(serverSeed, clientSeed, nonce + 2, 6) + 1;

        List<Integer> dice = Arrays.asList(die1, die2, die3);
        int total = die1 + die2 + die3;

        // Evaluate each bet
        Map<BetType, BetResult> results = new HashMap<>();
        BigDecimal totalBet = BigDecimal.ZERO;
        BigDecimal totalPayout = BigDecimal.ZERO;

        for (Map.Entry<BetType, BigDecimal> entry : bets.entrySet()) {
            BetType betType = entry.getKey();
            BigDecimal betAmount = entry.getValue();
            totalBet = totalBet.add(betAmount);

            BetResult result = evaluateBet(betType, betAmount, dice, total);
            results.put(betType, result);
            totalPayout = totalPayout.add(result.payout);
        }

        BigDecimal profit = totalPayout.subtract(totalBet);
        boolean isWin = totalPayout.compareTo(totalBet) > 0;

        return SicBoResultData.builder()
            .dice(dice)
            .total(total)
            .bets(bets)
            .results(results)
            .totalBet(totalBet)
            .totalPayout(totalPayout)
            .profit(profit)
            .isWin(isWin)
            .serverSeed(serverSeed)
            .clientSeed(clientSeed)
            .nonce(nonce)
            .build();
    }

    /**
     * Evaluate a single bet
     */
    private BetResult evaluateBet(BetType betType, BigDecimal betAmount, List<Integer> dice, int total) {
        boolean wins = false;
        double multiplier = 0;

        // Check if any triple (loses all small/big bets)
        boolean isAnyTriple = dice.get(0).equals(dice.get(1)) && dice.get(1).equals(dice.get(2));

        switch (betType) {
            // Small/Big bets
            case SMALL:
                wins = total >= 4 && total <= 10 && !isAnyTriple;
                multiplier = wins ? betType.payoutMultiplier : 0;
                break;
            case BIG:
                wins = total >= 11 && total <= 17 && !isAnyTriple;
                multiplier = wins ? betType.payoutMultiplier : 0;
                break;

            // Total bets
            case TOTAL_4: wins = total == 4; break;
            case TOTAL_5: wins = total == 5; break;
            case TOTAL_6: wins = total == 6; break;
            case TOTAL_7: wins = total == 7; break;
            case TOTAL_8: wins = total == 8; break;
            case TOTAL_9: wins = total == 9; break;
            case TOTAL_10: wins = total == 10; break;
            case TOTAL_11: wins = total == 11; break;
            case TOTAL_12: wins = total == 12; break;
            case TOTAL_13: wins = total == 13; break;
            case TOTAL_14: wins = total == 14; break;
            case TOTAL_15: wins = total == 15; break;
            case TOTAL_16: wins = total == 16; break;
            case TOTAL_17: wins = total == 17; break;

            // Single number bets
            case SINGLE_1: return evaluateSingleBet(1, betAmount, dice);
            case SINGLE_2: return evaluateSingleBet(2, betAmount, dice);
            case SINGLE_3: return evaluateSingleBet(3, betAmount, dice);
            case SINGLE_4: return evaluateSingleBet(4, betAmount, dice);
            case SINGLE_5: return evaluateSingleBet(5, betAmount, dice);
            case SINGLE_6: return evaluateSingleBet(6, betAmount, dice);

            // Double bets
            case DOUBLE_1: wins = Collections.frequency(dice, 1) >= 2; break;
            case DOUBLE_2: wins = Collections.frequency(dice, 2) >= 2; break;
            case DOUBLE_3: wins = Collections.frequency(dice, 3) >= 2; break;
            case DOUBLE_4: wins = Collections.frequency(dice, 4) >= 2; break;
            case DOUBLE_5: wins = Collections.frequency(dice, 5) >= 2; break;
            case DOUBLE_6: wins = Collections.frequency(dice, 6) >= 2; break;

            // Triple bets
            case ANY_TRIPLE: wins = isAnyTriple; break;
            case TRIPLE_1: wins = isAnyTriple && dice.get(0) == 1; break;
            case TRIPLE_2: wins = isAnyTriple && dice.get(0) == 2; break;
            case TRIPLE_3: wins = isAnyTriple && dice.get(0) == 3; break;
            case TRIPLE_4: wins = isAnyTriple && dice.get(0) == 4; break;
            case TRIPLE_5: wins = isAnyTriple && dice.get(0) == 5; break;
            case TRIPLE_6: wins = isAnyTriple && dice.get(0) == 6; break;

            // Combination bets (two specific numbers appear)
            default:
                if (betType.name().startsWith("COMBO_")) {
                    String[] parts = betType.name().split("_");
                    int num1 = Integer.parseInt(parts[1]);
                    int num2 = Integer.parseInt(parts[2]);
                    wins = dice.contains(num1) && dice.contains(num2);
                }
                break;
        }

        if (multiplier == 0) {
            multiplier = wins ? betType.payoutMultiplier : 0;
        }

        BigDecimal payout = betAmount.multiply(BigDecimal.valueOf(multiplier))
            .setScale(2, RoundingMode.HALF_UP);

        return new BetResult(wins, multiplier, payout);
    }

    /**
     * Evaluate single number bet (special case - pays more for multiple matches)
     */
    private BetResult evaluateSingleBet(int number, BigDecimal betAmount, List<Integer> dice) {
        int count = Collections.frequency(dice, number);
        double multiplier = 0;

        if (count == 1) {
            multiplier = 2.0;  // Pays 1:1 (return bet + winnings)
        } else if (count == 2) {
            multiplier = 3.0;  // Pays 2:1
        } else if (count == 3) {
            multiplier = 4.0;  // Pays 3:1
        }

        BigDecimal payout = betAmount.multiply(BigDecimal.valueOf(multiplier))
            .setScale(2, RoundingMode.HALF_UP);

        return new BetResult(count > 0, multiplier, payout);
    }

    /**
     * Bet result
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class BetResult {
        private Boolean wins;
        private Double multiplier;
        private BigDecimal payout;
    }
}
