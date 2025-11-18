package com.casino.game.engine;

import com.casino.game.dto.KenoResultData;
import com.casino.game.service.RngService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Keno Game Engine
 * Lottery-style number game (1-80, pick 1-10 numbers)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KenoGameEngine {

    private final RngService rngService;

    private static final double HOUSE_EDGE = 0.25; // 25% house edge (typical for Keno)
    private static final int MAX_NUMBER = 80;      // Numbers 1-80
    private static final int DRAW_COUNT = 20;      // Draw 20 numbers per round
    private static final int MIN_PICKS = 1;
    private static final int MAX_PICKS = 10;

    // Payout table (matches -> multiplier) based on number of picks
    // Format: payoutTables[numPicks - 1][matchesHit]
    private static final int[][] PAYOUT_TABLE = {
        // 1 pick
        {0, 3},
        // 2 picks
        {0, 0, 15},
        // 3 picks
        {0, 0, 2, 50},
        // 4 picks
        {0, 0, 1, 5, 100},
        // 5 picks
        {0, 0, 0, 3, 15, 500},
        // 6 picks
        {0, 0, 0, 2, 5, 100, 1500},
        // 7 picks
        {0, 0, 0, 1, 3, 20, 400, 5000},
        // 8 picks
        {0, 0, 0, 0, 2, 10, 50, 1000, 10000},
        // 9 picks
        {0, 0, 0, 0, 1, 5, 25, 200, 2500, 15000},
        // 10 picks
        {0, 0, 0, 0, 0, 2, 10, 50, 500, 5000, 25000}
    };

    /**
     * Play a round of Keno
     */
    public KenoResultData play(String serverSeed, String clientSeed, long nonce,
                                List<Integer> pickedNumbers, BigDecimal betAmount) {

        // Validate picks
        if (pickedNumbers == null || pickedNumbers.isEmpty()) {
            throw new IllegalArgumentException("Must pick at least 1 number");
        }
        if (pickedNumbers.size() > MAX_PICKS) {
            throw new IllegalArgumentException("Cannot pick more than " + MAX_PICKS + " numbers");
        }

        // Check for duplicates and valid range
        Set<Integer> uniquePicks = new HashSet<>(pickedNumbers);
        if (uniquePicks.size() != pickedNumbers.size()) {
            throw new IllegalArgumentException("Cannot pick duplicate numbers");
        }
        for (Integer num : pickedNumbers) {
            if (num < 1 || num > MAX_NUMBER) {
                throw new IllegalArgumentException("Numbers must be between 1 and " + MAX_NUMBER);
            }
        }

        // Draw 20 random numbers
        Set<Integer> drawnNumbers = new HashSet<>();
        for (int i = 0; i < DRAW_COUNT; i++) {
            int randomNum;
            do {
                randomNum = rngService.generateRandomNumber(
                    serverSeed,
                    clientSeed,
                    nonce + i,
                    MAX_NUMBER
                ) + 1; // +1 because we want 1-80, not 0-79
            } while (drawnNumbers.contains(randomNum));
            drawnNumbers.add(randomNum);
        }

        // Count matches
        int matchCount = 0;
        List<Integer> matchedNumbers = new ArrayList<>();
        for (Integer picked : pickedNumbers) {
            if (drawnNumbers.contains(picked)) {
                matchCount++;
                matchedNumbers.add(picked);
            }
        }

        // Calculate payout
        int multiplier = getPayoutMultiplier(pickedNumbers.size(), matchCount);
        BigDecimal payout = betAmount.multiply(BigDecimal.valueOf(multiplier))
            .setScale(2, RoundingMode.HALF_UP);

        BigDecimal profit = payout.subtract(betAmount);
        boolean isWin = payout.compareTo(betAmount) > 0;

        // Sort numbers for display
        List<Integer> sortedDrawn = new ArrayList<>(drawnNumbers);
        Collections.sort(sortedDrawn);
        Collections.sort(matchedNumbers);

        return KenoResultData.builder()
            .pickedNumbers(pickedNumbers)
            .drawnNumbers(sortedDrawn)
            .matchedNumbers(matchedNumbers)
            .matchCount(matchCount)
            .multiplier(multiplier)
            .payout(payout)
            .profit(profit)
            .isWin(isWin)
            .serverSeed(serverSeed)
            .clientSeed(clientSeed)
            .nonce(nonce)
            .build();
    }

    /**
     * Get payout multiplier from table
     */
    private int getPayoutMultiplier(int pickCount, int matchCount) {
        if (pickCount < MIN_PICKS || pickCount > MAX_PICKS) {
            return 0;
        }

        int[] payouts = PAYOUT_TABLE[pickCount - 1];
        if (matchCount >= payouts.length) {
            return 0;
        }

        return payouts[matchCount];
    }

    /**
     * Get theoretical return to player for given pick count
     */
    public double getRTP(int pickCount) {
        // Simplified RTP calculation - actual RTP varies by pick count
        return 1.0 - HOUSE_EDGE;
    }
}
