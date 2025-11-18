package com.casino.game.service;

import com.casino.game.dto.DiceGameResultData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiceGameEngine {

    private final RngService rngService;

    private static final int MAX_DICE_VALUE = 100;

    /**
     * Roll the dice
     * Player wins if result is over/under target based on prediction
     */
    public DiceGameResultData roll(
        String serverSeed,
        String clientSeed,
        long nonce,
        BigDecimal betAmount,
        int targetNumber,
        boolean rollOver
    ) {
        // Validate target
        if (targetNumber < 2 || targetNumber > 98) {
            throw new IllegalArgumentException("Target must be between 2 and 98");
        }

        // Generate dice result (0-99, display as 0.00-99.99)
        int result = rngService.generateRandomNumber(serverSeed, clientSeed, nonce, MAX_DICE_VALUE);

        // Check if player wins
        boolean isWin = rollOver ? (result > targetNumber) : (result < targetNumber);

        // Calculate multiplier based on win chance
        BigDecimal winChance = rollOver ?
            BigDecimal.valueOf(MAX_DICE_VALUE - targetNumber).divide(BigDecimal.valueOf(MAX_DICE_VALUE), 4, RoundingMode.HALF_UP) :
            BigDecimal.valueOf(targetNumber).divide(BigDecimal.valueOf(MAX_DICE_VALUE), 4, RoundingMode.HALF_UP);

        // House edge 1%
        BigDecimal multiplier = BigDecimal.ONE.divide(winChance, 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("0.99"));

        BigDecimal payout = isWin ? betAmount.multiply(multiplier) : BigDecimal.ZERO;
        BigDecimal netProfit = payout.subtract(betAmount);

        return DiceGameResultData.builder()
            .result(result)
            .targetNumber(targetNumber)
            .rollOver(rollOver)
            .isWin(isWin)
            .betAmount(betAmount)
            .multiplier(multiplier)
            .payout(payout)
            .netProfit(netProfit)
            .winChance(winChance.multiply(BigDecimal.valueOf(100)))
            .serverSeed(serverSeed)
            .nonce(nonce)
            .build();
    }

    /**
     * Calculate potential multiplier for given target and direction
     */
    public BigDecimal calculateMultiplier(int targetNumber, boolean rollOver) {
        BigDecimal winChance = rollOver ?
            BigDecimal.valueOf(MAX_DICE_VALUE - targetNumber).divide(BigDecimal.valueOf(MAX_DICE_VALUE), 4, RoundingMode.HALF_UP) :
            BigDecimal.valueOf(targetNumber).divide(BigDecimal.valueOf(MAX_DICE_VALUE), 4, RoundingMode.HALF_UP);

        return BigDecimal.ONE.divide(winChance, 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("0.99"));
    }
}
