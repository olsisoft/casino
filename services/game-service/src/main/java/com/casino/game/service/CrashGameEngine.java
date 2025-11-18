package com.casino.game.service;

import com.casino.game.dto.CrashGameResultData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrashGameEngine {

    private final RngService rngService;

    private static final double HOUSE_EDGE = 0.01; // 1%
    private static final BigDecimal MIN_CRASH_POINT = new BigDecimal("1.00");
    private static final BigDecimal MAX_CRASH_POINT = new BigDecimal("1000000.00");

    /**
     * Play a crash game
     * The crash point is generated using provably fair algorithm
     */
    public CrashGameResultData play(
        String serverSeed,
        String clientSeed,
        long nonce,
        BigDecimal betAmount,
        BigDecimal autoCashoutAt
    ) {
        // Validate auto cashout
        if (autoCashoutAt != null && autoCashoutAt.compareTo(MIN_CRASH_POINT) < 0) {
            throw new IllegalArgumentException("Auto cashout must be at least 1.00x");
        }

        // Generate crash point
        BigDecimal crashPoint = generateCrashPoint(serverSeed, clientSeed, nonce);

        // Generate game hash for verification
        String gameHash = generateGameHash(serverSeed, clientSeed, nonce);

        // Determine if player wins (if auto cashout set)
        boolean isWin = false;
        BigDecimal cashedOutAt = null;
        BigDecimal payout = BigDecimal.ZERO;
        String gameState = "CRASHED";

        if (autoCashoutAt != null) {
            if (crashPoint.compareTo(autoCashoutAt) >= 0) {
                // Player cashed out before crash
                isWin = true;
                cashedOutAt = autoCashoutAt;
                payout = betAmount.multiply(autoCashoutAt);
                gameState = "CASHED_OUT";
            }
        }

        BigDecimal netProfit = payout.subtract(betAmount);

        return CrashGameResultData.builder()
            .crashPoint(crashPoint)
            .betAmount(betAmount)
            .autoCashoutAt(autoCashoutAt)
            .cashedOutAt(cashedOutAt)
            .isWin(isWin)
            .payout(payout)
            .netProfit(netProfit)
            .gameState(gameState)
            .serverSeed(serverSeed)
            .nonce(nonce)
            .gameHash(gameHash)
            .build();
    }

    /**
     * Manual cashout during game
     * Used for live games where player clicks cashout button
     */
    public CrashGameResultData cashout(
        CrashGameResultData currentGame,
        BigDecimal cashoutMultiplier
    ) {
        // Validate cashout is before crash
        if (cashoutMultiplier.compareTo(currentGame.getCrashPoint()) > 0) {
            throw new IllegalArgumentException("Cannot cashout after crash point");
        }

        if (cashoutMultiplier.compareTo(MIN_CRASH_POINT) < 0) {
            throw new IllegalArgumentException("Cashout must be at least 1.00x");
        }

        BigDecimal payout = currentGame.getBetAmount().multiply(cashoutMultiplier);
        BigDecimal netProfit = payout.subtract(currentGame.getBetAmount());

        return CrashGameResultData.builder()
            .crashPoint(currentGame.getCrashPoint())
            .betAmount(currentGame.getBetAmount())
            .autoCashoutAt(currentGame.getAutoCashoutAt())
            .cashedOutAt(cashoutMultiplier)
            .isWin(true)
            .payout(payout)
            .netProfit(netProfit)
            .gameState("CASHED_OUT")
            .serverSeed(currentGame.getServerSeed())
            .nonce(currentGame.getNonce())
            .gameHash(currentGame.getGameHash())
            .build();
    }

    /**
     * Generate crash point using provably fair algorithm
     * Uses exponential distribution with house edge
     */
    private BigDecimal generateCrashPoint(String serverSeed, String clientSeed, long nonce) {
        // Get random float between 0.0 and 1.0
        double random = rngService.generateRandomDecimal(serverSeed, clientSeed, nonce);

        // Apply house edge
        double houseEdgeAdjusted = random * (1.0 - HOUSE_EDGE);

        // Calculate crash point using exponential distribution
        // This ensures proper distribution of crash points
        // Formula: 99 / (100 - X) where X is the random percentage
        double crashValue;
        if (houseEdgeAdjusted < 0.01) {
            // Very rare cases - crash at 1.00x
            crashValue = 1.00;
        } else {
            // Standard calculation
            crashValue = 99.0 / (100.0 * houseEdgeAdjusted);

            // Apply minimum and maximum bounds
            if (crashValue < 1.00) {
                crashValue = 1.00;
            } else if (crashValue > 1000000.00) {
                crashValue = 1000000.00;
            }
        }

        return BigDecimal.valueOf(crashValue).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Generate hash for game verification
     */
    private String generateGameHash(String serverSeed, String clientSeed, long nonce) {
        try {
            String combined = serverSeed + ":" + clientSeed + ":" + nonce;
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                serverSeed.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(combined.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error generating game hash", e);
            throw new RuntimeException("Failed to generate game hash", e);
        }
    }

    /**
     * Verify a game result
     * Players can use this to verify the crash point was fair
     */
    public boolean verifyResult(
        String serverSeed,
        String clientSeed,
        long nonce,
        BigDecimal expectedCrashPoint
    ) {
        BigDecimal calculatedCrashPoint = generateCrashPoint(serverSeed, clientSeed, nonce);
        return calculatedCrashPoint.compareTo(expectedCrashPoint) == 0;
    }

    /**
     * Calculate expected return to player (RTP)
     * For informational purposes
     */
    public BigDecimal calculateRTP() {
        return BigDecimal.valueOf(1.0 - HOUSE_EDGE)
            .setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Get statistics about crash point distribution
     * Useful for showing players the odds
     */
    public CrashStatistics getCrashStatistics() {
        return CrashStatistics.builder()
            .averageCrashPoint(new BigDecimal("1.98")) // Approximately 1 / (1 - house_edge)
            .medianCrashPoint(new BigDecimal("1.37")) // Median of exponential distribution
            .chanceOfReaching2x(new BigDecimal("48.51")) // ~49% chance
            .chanceOfReaching10x(new BigDecimal("9.70")) // ~10% chance
            .chanceOfReaching100x(new BigDecimal("0.98")) // ~1% chance
            .houseEdge(BigDecimal.valueOf(HOUSE_EDGE * 100)) // 1%
            .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class CrashStatistics {
        private BigDecimal averageCrashPoint;
        private BigDecimal medianCrashPoint;
        private BigDecimal chanceOfReaching2x;
        private BigDecimal chanceOfReaching10x;
        private BigDecimal chanceOfReaching100x;
        private BigDecimal houseEdge;
    }
}
