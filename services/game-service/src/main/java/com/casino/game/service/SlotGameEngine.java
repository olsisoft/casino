package com.casino.game.service;

import com.casino.game.dto.SlotResultData;
import com.casino.game.entity.GameConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlotGameEngine {

    private final RngService rngService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Standard slot symbols with their weights (probability of appearing)
    private static final Map<String, Double> SYMBOL_WEIGHTS = Map.of(
        "SEVEN", 2.0,      // Rarest - highest payout
        "BAR", 5.0,
        "CHERRY", 10.0,
        "BELL", 15.0,
        "LEMON", 20.0,
        "ORANGE", 20.0,
        "PLUM", 20.0,
        "GRAPE", 25.0,
        "WATERMELON", 25.0,
        "STAR", 8.0        // Scatter/Bonus symbol
    );

    // Payout multipliers for matching symbols (based on count)
    private static final Map<String, Map<Integer, Integer>> SYMBOL_PAYOUTS = Map.of(
        "SEVEN", Map.of(3, 100, 4, 500, 5, 2000),
        "BAR", Map.of(3, 50, 4, 200, 5, 1000),
        "CHERRY", Map.of(2, 5, 3, 20, 4, 100, 5, 500),
        "BELL", Map.of(3, 15, 4, 75, 5, 300),
        "LEMON", Map.of(3, 10, 4, 50, 5, 200),
        "ORANGE", Map.of(3, 10, 4, 50, 5, 200),
        "PLUM", Map.of(3, 10, 4, 50, 5, 200),
        "GRAPE", Map.of(3, 8, 4, 40, 5, 150),
        "WATERMELON", Map.of(3, 8, 4, 40, 5, 150),
        "STAR", Map.of(3, 2, 4, 10, 5, 50) // Scatter pays anywhere
    );

    // Standard 5x3 slot configuration with 20 paylines
    private static final int DEFAULT_REELS = 5;
    private static final int DEFAULT_ROWS = 3;
    private static final int DEFAULT_PAYLINES = 20;

    /**
     * Spin the slot machine and generate result
     */
    public SlotResultData spin(
        GameConfig gameConfig,
        BigDecimal betAmount,
        String serverSeed,
        String clientSeed,
        long nonce
    ) {
        int reels = gameConfig.getReels() != null ? gameConfig.getReels() : DEFAULT_REELS;
        int rows = gameConfig.getRows() != null ? gameConfig.getRows() : DEFAULT_ROWS;
        int paylines = gameConfig.getPaylines() != null ? gameConfig.getPaylines() : DEFAULT_PAYLINES;

        // Generate reel results
        List<List<String>> reelResults = generateReels(reels, rows, serverSeed, clientSeed, nonce);

        // Check for wins on all paylines
        List<SlotResultData.WinLine> winLines = checkWinLines(reelResults, betAmount, paylines);

        // Check for scatter wins (stars)
        checkScatterWins(reelResults, betAmount, winLines);

        // Check if bonus feature is triggered (3+ scatter symbols)
        boolean bonusTriggered = countScatters(reelResults) >= 3;
        Integer freeSpinsAwarded = bonusTriggered ? 10 : null;

        return SlotResultData.builder()
            .reels(reelResults)
            .winLines(winLines)
            .bonusTriggered(bonusTriggered)
            .freeSpinsAwarded(freeSpinsAwarded)
            .build();
    }

    /**
     * Generate random symbols for all reels
     */
    private List<List<String>> generateReels(
        int reelCount,
        int rowCount,
        String serverSeed,
        String clientSeed,
        long baseNonce
    ) {
        List<List<String>> reels = new ArrayList<>();
        List<String> symbols = new ArrayList<>(SYMBOL_WEIGHTS.keySet());
        double[] weights = symbols.stream()
            .mapToDouble(SYMBOL_WEIGHTS::get)
            .toArray();

        for (int reel = 0; reel < reelCount; reel++) {
            List<String> reelSymbols = new ArrayList<>();
            for (int row = 0; row < rowCount; row++) {
                long currentNonce = baseNonce + (reel * rowCount) + row;
                int symbolIndex = rngService.generateWeightedRandom(
                    serverSeed,
                    clientSeed,
                    currentNonce,
                    weights
                );
                reelSymbols.add(symbols.get(symbolIndex));
            }
            reels.add(reelSymbols);
        }

        return reels;
    }

    /**
     * Check all paylines for winning combinations
     */
    private List<SlotResultData.WinLine> checkWinLines(
        List<List<String>> reels,
        BigDecimal betPerLine,
        int paylineCount
    ) {
        List<SlotResultData.WinLine> winLines = new ArrayList<>();

        // Standard payline patterns (simplified - normally these would be more complex)
        for (int line = 0; line < Math.min(paylineCount, 20); line++) {
            SlotResultData.WinLine winLine = checkPayline(reels, line, betPerLine);
            if (winLine != null) {
                winLines.add(winLine);
            }
        }

        return winLines;
    }

    /**
     * Check a specific payline for wins
     * Simplified payline check - in reality, each payline has a specific pattern
     */
    private SlotResultData.WinLine checkPayline(
        List<List<String>> reels,
        int lineNumber,
        BigDecimal betPerLine
    ) {
        // For simplicity, we'll check the middle row (row 1) for the first payline
        // and vary the row pattern based on line number
        int[] rowPattern = getRowPattern(lineNumber);

        List<String> symbols = new ArrayList<>();
        for (int i = 0; i < Math.min(reels.size(), rowPattern.length); i++) {
            int row = rowPattern[i];
            if (row < reels.get(i).size()) {
                symbols.add(reels.get(i).get(row));
            }
        }

        // Count matching symbols from left to right
        if (symbols.isEmpty()) return null;

        String firstSymbol = symbols.get(0);
        if ("STAR".equals(firstSymbol)) return null; // Scatters don't pay on lines

        int matchCount = 1;
        for (int i = 1; i < symbols.size(); i++) {
            if (symbols.get(i).equals(firstSymbol)) {
                matchCount++;
            } else {
                break;
            }
        }

        // Check if we have a payout for this match
        Map<Integer, Integer> symbolPayout = SYMBOL_PAYOUTS.get(firstSymbol);
        if (symbolPayout == null || !symbolPayout.containsKey(matchCount)) {
            return null;
        }

        int multiplier = symbolPayout.get(matchCount);
        BigDecimal payout = betPerLine.multiply(BigDecimal.valueOf(multiplier));

        return SlotResultData.WinLine.builder()
            .lineNumber(lineNumber)
            .symbol(firstSymbol)
            .count(matchCount)
            .payout(payout)
            .build();
    }

    /**
     * Get row pattern for a payline
     * Returns array of row indices for each reel
     */
    private int[] getRowPattern(int lineNumber) {
        // Define 20 different payline patterns
        return switch (lineNumber) {
            case 0 -> new int[]{1, 1, 1, 1, 1}; // Middle row
            case 1 -> new int[]{0, 0, 0, 0, 0}; // Top row
            case 2 -> new int[]{2, 2, 2, 2, 2}; // Bottom row
            case 3 -> new int[]{0, 1, 2, 1, 0}; // V shape
            case 4 -> new int[]{2, 1, 0, 1, 2}; // Inverted V
            case 5 -> new int[]{0, 0, 1, 0, 0}; // Top dip
            case 6 -> new int[]{2, 2, 1, 2, 2}; // Bottom dip
            case 7 -> new int[]{1, 0, 0, 0, 1}; // Top triangle
            case 8 -> new int[]{1, 2, 2, 2, 1}; // Bottom triangle
            case 9 -> new int[]{0, 1, 1, 1, 0}; // Middle plateau
            case 10 -> new int[]{2, 1, 1, 1, 2}; // Middle valley
            case 11 -> new int[]{1, 0, 1, 0, 1}; // Zig-zag up
            case 12 -> new int[]{1, 2, 1, 2, 1}; // Zig-zag down
            case 13 -> new int[]{0, 1, 0, 1, 0}; // Wave up
            case 14 -> new int[]{2, 1, 2, 1, 2}; // Wave down
            case 15 -> new int[]{1, 1, 0, 1, 1}; // Middle with top dip
            case 16 -> new int[]{1, 1, 2, 1, 1}; // Middle with bottom dip
            case 17 -> new int[]{0, 2, 0, 2, 0}; // Diagonal alternate
            case 18 -> new int[]{2, 0, 2, 0, 2}; // Diagonal alternate reverse
            default -> new int[]{0, 1, 2, 1, 0}; // V shape (default)
        };
    }

    /**
     * Check for scatter symbol wins (pay anywhere on reels)
     */
    private void checkScatterWins(
        List<List<String>> reels,
        BigDecimal betAmount,
        List<SlotResultData.WinLine> winLines
    ) {
        int scatterCount = countScatters(reels);

        if (scatterCount >= 3) {
            Map<Integer, Integer> scatterPayouts = SYMBOL_PAYOUTS.get("STAR");
            if (scatterPayouts.containsKey(scatterCount)) {
                int multiplier = scatterPayouts.get(scatterCount);
                BigDecimal payout = betAmount.multiply(BigDecimal.valueOf(multiplier));

                winLines.add(SlotResultData.WinLine.builder()
                    .lineNumber(-1) // -1 indicates scatter win
                    .symbol("STAR")
                    .count(scatterCount)
                    .payout(payout)
                    .build());
            }
        }
    }

    /**
     * Count total scatter symbols on all reels
     */
    private int countScatters(List<List<String>> reels) {
        return (int) reels.stream()
            .flatMap(List::stream)
            .filter(symbol -> "STAR".equals(symbol))
            .count();
    }

    /**
     * Calculate total payout from all win lines
     */
    public BigDecimal calculateTotalPayout(SlotResultData result) {
        return result.getWinLines().stream()
            .map(SlotResultData.WinLine::getPayout)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.DOWN);
    }

    /**
     * Convert SlotResultData to JSON string
     */
    public String toJson(SlotResultData data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("Error converting slot result to JSON: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * Calculate RTP (Return to Player) percentage for verification
     */
    public BigDecimal calculateRTP(List<BigDecimal> bets, List<BigDecimal> wins) {
        BigDecimal totalBet = bets.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalWin = wins.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalBet.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalWin.divide(totalBet, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
}
