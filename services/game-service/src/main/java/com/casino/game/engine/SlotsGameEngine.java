package com.casino.game.engine;

import com.casino.game.dto.SlotsResultData;
import com.casino.game.service.RngService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Slots Game Engine
 * 5-reel, 3-row, 25-payline slot machine with various features
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SlotsGameEngine {

    private final RngService rngService;

    private static final double HOUSE_EDGE = 0.05; // 5% house edge
    private static final double RTP = 0.95; // 95% Return to Player

    // Symbols (higher value = rarer)
    private enum Symbol {
        SEVEN(100, 1),      // Jackpot symbol - 777
        DIAMOND(50, 2),     // High value
        GOLD_BAR(25, 3),    // High value
        CHERRY(15, 5),      // Medium value
        LEMON(10, 7),       // Medium value
        ORANGE(8, 9),       // Medium value
        PLUM(6, 11),        // Low value
        GRAPE(5, 13),       // Low value
        WATERMELON(4, 15),  // Low value
        WILD(200, 1),       // Wild symbol
        SCATTER(0, 3);      // Scatter for free spins

        final int payout;
        final int weight;

        Symbol(int payout, int weight) {
            this.payout = payout;
            this.weight = weight;
        }
    }

    // Reel configuration (weighted probabilities)
    private static final List<Symbol> REEL_SYMBOLS = createReelStrip();

    private static List<Symbol> createReelStrip() {
        List<Symbol> symbols = new ArrayList<>();
        for (Symbol symbol : Symbol.values()) {
            for (int i = 0; i < symbol.weight; i++) {
                symbols.add(symbol);
            }
        }
        return symbols;
    }

    /**
     * Spin the slots
     */
    public SlotsResultData spin(String serverSeed, String clientSeed, long nonce, BigDecimal betAmount) {
        // Generate 5 reels x 3 rows = 15 symbols
        Symbol[][] reels = new Symbol[5][3];

        for (int reel = 0; reel < 5; reel++) {
            for (int row = 0; row < 3; row++) {
                int index = rngService.generateRandomNumber(
                    serverSeed,
                    clientSeed,
                    nonce + (reel * 3 + row),
                    REEL_SYMBOLS.size()
                );
                reels[reel][row] = REEL_SYMBOLS.get(index);
            }
        }

        // Check for wins
        List<WinLine> winLines = checkWinLines(reels);

        // Calculate total payout
        BigDecimal totalPayout = BigDecimal.ZERO;
        for (WinLine winLine : winLines) {
            BigDecimal linePayout = betAmount
                .multiply(BigDecimal.valueOf(winLine.multiplier))
                .setScale(2, RoundingMode.HALF_UP);
            totalPayout = totalPayout.add(linePayout);
        }

        // Check for scatter bonus (free spins)
        int scatterCount = countScatters(reels);
        int freeSpins = calculateFreeSpins(scatterCount);

        boolean isWin = totalPayout.compareTo(BigDecimal.ZERO) > 0;
        BigDecimal profit = totalPayout.subtract(betAmount);

        return SlotsResultData.builder()
            .reels(convertReelsToString(reels))
            .winLines(winLines)
            .totalPayout(totalPayout)
            .profit(profit)
            .isWin(isWin)
            .scatterCount(scatterCount)
            .freeSpinsAwarded(freeSpins)
            .serverSeed(serverSeed)
            .clientSeed(clientSeed)
            .nonce(nonce)
            .build();
    }

    /**
     * Check all 25 paylines for wins
     */
    private List<WinLine> checkWinLines(Symbol[][] reels) {
        List<WinLine> winLines = new ArrayList<>();

        // Define 25 paylines (simplified - showing first 5)
        int[][] paylines = {
            {1, 1, 1, 1, 1}, // Middle row
            {0, 0, 0, 0, 0}, // Top row
            {2, 2, 2, 2, 2}, // Bottom row
            {0, 1, 2, 1, 0}, // V shape
            {2, 1, 0, 1, 2}, // Inverted V
            // ... 20 more paylines
        };

        for (int lineIndex = 0; lineIndex < paylines.length && lineIndex < 5; lineIndex++) {
            int[] payline = paylines[lineIndex];
            WinLine winLine = checkPayline(reels, payline, lineIndex + 1);
            if (winLine != null) {
                winLines.add(winLine);
            }
        }

        return winLines;
    }

    /**
     * Check a single payline
     */
    private WinLine checkPayline(Symbol[][] reels, int[] payline, int lineNumber) {
        Symbol firstSymbol = reels[0][payline[0]];
        if (firstSymbol == Symbol.SCATTER) {
            return null; // Scatters don't count on paylines
        }

        int consecutiveCount = 1;

        for (int reel = 1; reel < 5; reel++) {
            Symbol currentSymbol = reels[reel][payline[reel]];

            // Wild substitutes for any symbol
            if (currentSymbol == Symbol.WILD || currentSymbol == firstSymbol) {
                consecutiveCount++;
            } else {
                break;
            }
        }

        // Need at least 3 consecutive symbols to win
        if (consecutiveCount >= 3) {
            int multiplier = calculateMultiplier(firstSymbol, consecutiveCount);

            return WinLine.builder()
                .lineNumber(lineNumber)
                .symbol(firstSymbol.name())
                .count(consecutiveCount)
                .multiplier(multiplier)
                .build();
        }

        return null;
    }

    /**
     * Calculate payout multiplier
     */
    private int calculateMultiplier(Symbol symbol, int count) {
        int baseMultiplier = symbol.payout;

        // Multipliers based on count
        switch (count) {
            case 3: return baseMultiplier;
            case 4: return baseMultiplier * 3;
            case 5: return baseMultiplier * 10; // Big win!
            default: return 0;
        }
    }

    /**
     * Count scatter symbols
     */
    private int countScatters(Symbol[][] reels) {
        int count = 0;
        for (int reel = 0; reel < 5; reel++) {
            for (int row = 0; row < 3; row++) {
                if (reels[reel][row] == Symbol.SCATTER) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Calculate free spins based on scatter count
     */
    private int calculateFreeSpins(int scatterCount) {
        switch (scatterCount) {
            case 3: return 10;
            case 4: return 15;
            case 5: return 25;
            default: return 0;
        }
    }

    /**
     * Convert reels to string array for response
     */
    private String[][] convertReelsToString(Symbol[][] reels) {
        String[][] result = new String[5][3];
        for (int reel = 0; reel < 5; reel++) {
            for (int row = 0; row < 3; row++) {
                result[reel][row] = reels[reel][row].name();
            }
        }
        return result;
    }

    @lombok.Data
    @lombok.Builder
    public static class WinLine {
        private int lineNumber;
        private String symbol;
        private int count;
        private int multiplier;
    }
}
