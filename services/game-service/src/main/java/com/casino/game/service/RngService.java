package com.casino.game.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Random Number Generator Service for Provably Fair Gaming
 * Uses HMAC-SHA256 for generating deterministic but unpredictable results
 */
@Slf4j
@Service
public class RngService {

    private final SecureRandom secureRandom = new SecureRandom();
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /**
     * Generate a server seed for a game round
     */
    public String generateServerSeed() {
        byte[] seedBytes = new byte[32];
        secureRandom.nextBytes(seedBytes);
        return Base64.getEncoder().encodeToString(seedBytes);
    }

    /**
     * Generate a random number between 0 and max (exclusive) using provably fair method
     *
     * @param serverSeed Server-generated seed
     * @param clientSeed Client-provided seed (can be null)
     * @param nonce Round counter to ensure uniqueness
     * @param max Maximum value (exclusive)
     * @return Random number between 0 and max
     */
    public int generateRandomNumber(String serverSeed, String clientSeed, long nonce, int max) {
        if (max <= 0) {
            throw new IllegalArgumentException("Max must be positive");
        }

        byte[] hash = generateHash(serverSeed, clientSeed, nonce);

        // Convert first 4 bytes of hash to integer
        int value = 0;
        for (int i = 0; i < 4; i++) {
            value = (value << 8) | (hash[i] & 0xFF);
        }

        // Make it positive and within range
        return Math.abs(value) % max;
    }

    /**
     * Generate a random decimal between 0.0 and 1.0
     */
    public double generateRandomDecimal(String serverSeed, String clientSeed, long nonce) {
        byte[] hash = generateHash(serverSeed, clientSeed, nonce);

        // Convert first 8 bytes to long
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | (hash[i] & 0xFF);
        }

        // Normalize to 0.0 - 1.0
        return Math.abs(value) / (double) Long.MAX_VALUE;
    }

    /**
     * Generate multiple random numbers for a single round (e.g., slot reels)
     * Uses different nonces for each number to ensure uniqueness
     */
    public List<Integer> generateMultipleRandomNumbers(
        String serverSeed,
        String clientSeed,
        long baseNonce,
        int count,
        int max
    ) {
        List<Integer> numbers = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            numbers.add(generateRandomNumber(serverSeed, clientSeed, baseNonce + i, max));
        }
        return numbers;
    }

    /**
     * Generate HMAC-SHA256 hash
     */
    private byte[] generateHash(String serverSeed, String clientSeed, long nonce) {
        try {
            String message = serverSeed + ":" + (clientSeed != null ? clientSeed : "") + ":" + nonce;

            Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(
                serverSeed.getBytes(StandardCharsets.UTF_8),
                HMAC_ALGORITHM
            );
            hmac.init(keySpec);

            return hmac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error generating hash: {}", e.getMessage());
            throw new RuntimeException("Failed to generate random number", e);
        }
    }

    /**
     * Verify a result by regenerating it with the same seeds and nonce
     */
    public boolean verifyResult(
        String serverSeed,
        String clientSeed,
        long nonce,
        int expectedValue,
        int max
    ) {
        int actualValue = generateRandomNumber(serverSeed, clientSeed, nonce, max);
        return actualValue == expectedValue;
    }

    /**
     * Generate a weighted random selection (useful for different win probabilities)
     *
     * @param weights Array of weights for each option
     * @return Index of selected option
     */
    public int generateWeightedRandom(
        String serverSeed,
        String clientSeed,
        long nonce,
        double[] weights
    ) {
        double totalWeight = 0;
        for (double weight : weights) {
            totalWeight += weight;
        }

        double random = generateRandomDecimal(serverSeed, clientSeed, nonce) * totalWeight;

        double cumulative = 0;
        for (int i = 0; i < weights.length; i++) {
            cumulative += weights[i];
            if (random <= cumulative) {
                return i;
            }
        }

        return weights.length - 1;
    }

    /**
     * Shuffle an array using Fisher-Yates algorithm with provably fair RNG
     */
    public <T> List<T> shuffle(
        List<T> items,
        String serverSeed,
        String clientSeed,
        long baseNonce
    ) {
        List<T> result = new ArrayList<>(items);

        for (int i = result.size() - 1; i > 0; i--) {
            int j = generateRandomNumber(serverSeed, clientSeed, baseNonce + i, i + 1);
            T temp = result.get(i);
            result.set(i, result.get(j));
            result.set(j, temp);
        }

        return result;
    }
}
