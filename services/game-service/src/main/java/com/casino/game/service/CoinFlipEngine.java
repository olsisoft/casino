package com.casino.game.service;

import com.casino.game.dto.CoinFlipResultData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoinFlipEngine {

    private final RngService rngService;

    private static final double HOUSE_EDGE = 0.01; // 1%
    private static final BigDecimal BASE_MULTIPLIER = new BigDecimal("2.00"); // 2x for 50/50

    public enum CoinSide {
        HEADS, TAILS
    }

    /**
     * Flip a coin
     * Player wins if their choice matches the result
     */
    public CoinFlipResultData flip(
        String serverSeed,
        String clientSeed,
        long nonce,
        BigDecimal betAmount,
        CoinSide playerChoice
    ) {
        // Generate result (0 or 1, then map to HEADS/TAILS)
        int randomValue = rngService.generateRandomNumber(serverSeed, clientSeed, nonce, 2);
        CoinSide result = randomValue == 0 ? CoinSide.HEADS : CoinSide.TAILS;

        // Check if player wins
        boolean isWin = result == playerChoice;

        // Calculate payout with house edge
        BigDecimal multiplier = BASE_MULTIPLIER.multiply(
            BigDecimal.valueOf(1.0 - HOUSE_EDGE)
        ).setScale(2, RoundingMode.HALF_UP);

        BigDecimal payout = isWin ? betAmount.multiply(multiplier) : BigDecimal.ZERO;
        BigDecimal netProfit = payout.subtract(betAmount);

        return CoinFlipResultData.builder()
            .result(result.name())
            .playerChoice(playerChoice.name())
            .isWin(isWin)
            .betAmount(betAmount)
            .multiplier(multiplier)
            .payout(payout)
            .netProfit(netProfit)
            .serverSeed(serverSeed)
            .nonce(nonce)
            .build();
    }

    /**
     * Get the multiplier for coin flip (always same for 50/50)
     */
    public BigDecimal getMultiplier() {
        return BASE_MULTIPLIER.multiply(
            BigDecimal.valueOf(1.0 - HOUSE_EDGE)
        ).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Verify a coin flip result
     */
    public boolean verifyResult(
        String serverSeed,
        String clientSeed,
        long nonce,
        String expectedResult
    ) {
        int randomValue = rngService.generateRandomNumber(serverSeed, clientSeed, nonce, 2);
        CoinSide calculatedResult = randomValue == 0 ? CoinSide.HEADS : CoinSide.TAILS;
        return calculatedResult.name().equals(expectedResult);
    }
}
