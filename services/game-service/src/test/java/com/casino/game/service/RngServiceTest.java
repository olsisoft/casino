package com.casino.game.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RNG Service Tests - Provably Fair Gaming")
class RngServiceTest {

    private RngService rngService;

    @BeforeEach
    void setUp() {
        rngService = new RngService();
    }

    @Test
    @DisplayName("Should generate unique server seeds")
    void testGenerateServerSeed() {
        // Generate multiple seeds
        Set<String> seeds = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            seeds.add(rngService.generateServerSeed());
        }

        // All seeds should be unique
        assertEquals(100, seeds.size(), "All server seeds should be unique");

        // All seeds should be non-empty
        seeds.forEach(seed -> assertFalse(seed.isEmpty(), "Server seed should not be empty"));
    }

    @Test
    @DisplayName("Should generate deterministic random numbers with same inputs")
    void testDeterministicRandomNumber() {
        String serverSeed = "test-server-seed";
        String clientSeed = "test-client-seed";
        long nonce = 1L;
        int max = 10;

        // Generate same number multiple times
        int result1 = rngService.generateRandomNumber(serverSeed, clientSeed, nonce, max);
        int result2 = rngService.generateRandomNumber(serverSeed, clientSeed, nonce, max);
        int result3 = rngService.generateRandomNumber(serverSeed, clientSeed, nonce, max);

        // All results should be identical (deterministic)
        assertEquals(result1, result2, "Same inputs should produce same output");
        assertEquals(result2, result3, "Same inputs should produce same output");

        // Result should be within range
        assertTrue(result1 >= 0 && result1 < max, "Result should be within range [0, max)");
    }

    @Test
    @DisplayName("Should generate different numbers with different nonces")
    void testDifferentNonces() {
        String serverSeed = "test-server-seed";
        String clientSeed = "test-client-seed";
        int max = 100;

        // Generate with different nonces
        int result1 = rngService.generateRandomNumber(serverSeed, clientSeed, 1L, max);
        int result2 = rngService.generateRandomNumber(serverSeed, clientSeed, 2L, max);
        int result3 = rngService.generateRandomNumber(serverSeed, clientSeed, 3L, max);

        // Results should likely be different (not guaranteed but highly probable)
        assertNotEquals(result1, result2, "Different nonces should produce different results");
        assertNotEquals(result2, result3, "Different nonces should produce different results");
    }

    @Test
    @DisplayName("Should generate numbers within specified range")
    void testNumberRange() {
        String serverSeed = rngService.generateServerSeed();
        String clientSeed = "client-seed";

        // Test various ranges
        int[] maxValues = {10, 50, 100, 1000};

        for (int max : maxValues) {
            for (long nonce = 0; nonce < 100; nonce++) {
                int result = rngService.generateRandomNumber(serverSeed, clientSeed, nonce, max);
                assertTrue(result >= 0 && result < max,
                    String.format("Result %d should be in range [0, %d)", result, max));
            }
        }
    }

    @Test
    @DisplayName("Should generate random decimals between 0 and 1")
    void testRandomDecimal() {
        String serverSeed = rngService.generateServerSeed();
        String clientSeed = "client-seed";

        for (long nonce = 0; nonce < 100; nonce++) {
            double result = rngService.generateRandomDecimal(serverSeed, clientSeed, nonce);
            assertTrue(result >= 0.0 && result <= 1.0,
                String.format("Decimal %f should be between 0.0 and 1.0", result));
        }
    }

    @Test
    @DisplayName("Should generate multiple unique random numbers")
    void testMultipleRandomNumbers() {
        String serverSeed = rngService.generateServerSeed();
        String clientSeed = "client-seed";
        long baseNonce = 1L;
        int count = 5;
        int max = 10;

        List<Integer> results = rngService.generateMultipleRandomNumbers(
            serverSeed, clientSeed, baseNonce, count, max
        );

        // Should generate correct count
        assertEquals(count, results.size(), "Should generate correct number of values");

        // All should be within range
        results.forEach(result ->
            assertTrue(result >= 0 && result < max, "All values should be within range")
        );

        // Should be deterministic
        List<Integer> results2 = rngService.generateMultipleRandomNumbers(
            serverSeed, clientSeed, baseNonce, count, max
        );
        assertEquals(results, results2, "Same inputs should produce same sequence");
    }

    @Test
    @DisplayName("Should verify results correctly")
    void testVerifyResult() {
        String serverSeed = "test-server-seed";
        String clientSeed = "test-client-seed";
        long nonce = 123L;
        int max = 100;

        // Generate a result
        int expectedValue = rngService.generateRandomNumber(serverSeed, clientSeed, nonce, max);

        // Verify it
        boolean isValid = rngService.verifyResult(serverSeed, clientSeed, nonce, expectedValue, max);
        assertTrue(isValid, "Should verify correct result as valid");

        // Try with wrong value
        boolean isInvalid = rngService.verifyResult(serverSeed, clientSeed, nonce, expectedValue + 1, max);
        assertFalse(isInvalid, "Should verify incorrect result as invalid");
    }

    @Test
    @DisplayName("Should generate weighted random selections")
    void testWeightedRandom() {
        String serverSeed = "test-server-seed";
        String clientSeed = "client-seed";

        // Weights: 70%, 20%, 10%
        double[] weights = {70.0, 20.0, 10.0};

        // Generate many samples
        int[] counts = new int[3];
        int samples = 1000;

        for (long nonce = 0; nonce < samples; nonce++) {
            int result = rngService.generateWeightedRandom(serverSeed, clientSeed, nonce, weights);
            counts[result]++;
        }

        // Index 0 should be selected most often (~70%)
        // Index 1 should be selected ~20%
        // Index 2 should be selected ~10%

        double ratio0 = counts[0] / (double) samples;
        double ratio1 = counts[1] / (double) samples;
        double ratio2 = counts[2] / (double) samples;

        // Allow 15% margin of error
        assertTrue(ratio0 > 0.55 && ratio0 < 0.85, "Index 0 should be selected ~70% of time");
        assertTrue(ratio1 > 0.05 && ratio1 < 0.35, "Index 1 should be selected ~20% of time");
        assertTrue(ratio2 > 0.0 && ratio2 < 0.25, "Index 2 should be selected ~10% of time");
    }

    @Test
    @DisplayName("Should shuffle deterministically")
    void testShuffle() {
        List<String> items = List.of("A", "B", "C", "D", "E");
        String serverSeed = "test-server-seed";
        String clientSeed = "client-seed";
        long baseNonce = 1L;

        // Shuffle multiple times with same seeds
        List<String> shuffled1 = rngService.shuffle(items, serverSeed, clientSeed, baseNonce);
        List<String> shuffled2 = rngService.shuffle(items, serverSeed, clientSeed, baseNonce);

        // Should be deterministic
        assertEquals(shuffled1, shuffled2, "Same seeds should produce same shuffle");

        // Should contain all original items
        assertEquals(items.size(), shuffled1.size(), "Shuffled list should have same size");
        assertTrue(shuffled1.containsAll(items), "Shuffled list should contain all original items");

        // Should be different from original (high probability)
        assertNotEquals(items, shuffled1, "Shuffled list should be different from original");
    }

    @Test
    @DisplayName("Should handle null client seed")
    void testNullClientSeed() {
        String serverSeed = "test-server-seed";
        long nonce = 1L;
        int max = 10;

        // Should work with null client seed
        assertDoesNotThrow(() ->
            rngService.generateRandomNumber(serverSeed, null, nonce, max)
        );

        // Should be deterministic even with null
        int result1 = rngService.generateRandomNumber(serverSeed, null, nonce, max);
        int result2 = rngService.generateRandomNumber(serverSeed, null, nonce, max);
        assertEquals(result1, result2, "Should be deterministic even with null client seed");
    }

    @Test
    @DisplayName("Should throw exception for invalid max value")
    void testInvalidMaxValue() {
        String serverSeed = "test-server-seed";
        String clientSeed = "client-seed";
        long nonce = 1L;

        // Should throw for max <= 0
        assertThrows(IllegalArgumentException.class, () ->
            rngService.generateRandomNumber(serverSeed, clientSeed, nonce, 0)
        );

        assertThrows(IllegalArgumentException.class, () ->
            rngService.generateRandomNumber(serverSeed, clientSeed, nonce, -1)
        );
    }

    @Test
    @DisplayName("Should distribute uniformly across range")
    void testUniformDistribution() {
        String serverSeed = rngService.generateServerSeed();
        String clientSeed = "client-seed";
        int max = 10;
        int samples = 10000;

        // Count occurrences of each number
        int[] counts = new int[max];

        for (long nonce = 0; nonce < samples; nonce++) {
            int result = rngService.generateRandomNumber(serverSeed, clientSeed, nonce, max);
            counts[result]++;
        }

        // Each number should appear roughly samples/max times
        double expected = samples / (double) max;

        for (int i = 0; i < max; i++) {
            double ratio = counts[i] / expected;
            // Allow 30% deviation (statistical variance)
            assertTrue(ratio > 0.7 && ratio < 1.3,
                String.format("Number %d appeared %d times, expected ~%.0f (ratio: %.2f)",
                    i, counts[i], expected, ratio));
        }
    }
}
