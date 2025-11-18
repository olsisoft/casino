package com.casino.game.service;

import com.casino.game.dto.RouletteResultData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouletteEngine {

    private final RngService rngService;

    // European Roulette (0-36)
    private static final int EUROPEAN_MAX = 37;

    // American Roulette (0, 00, 1-36)
    private static final int AMERICAN_MAX = 38;

    // Red numbers in roulette
    private static final Set<Integer> RED_NUMBERS = Set.of(
        1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36
    );

    // Black numbers (all other numbers except 0 and 00)
    private static final Set<Integer> BLACK_NUMBERS = Set.of(
        2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35
    );

    /**
     * Spin the roulette wheel
     */
    public RouletteResultData spin(
        String serverSeed,
        String clientSeed,
        long nonce,
        Map<String, BigDecimal> bets,
        RouletteType type
    ) {
        // Generate winning number
        int maxNumber = type == RouletteType.EUROPEAN ? EUROPEAN_MAX : AMERICAN_MAX;
        int winningNumber = rngService.generateRandomNumber(serverSeed, clientSeed, nonce, maxNumber);

        // Calculate total bet and payout
        BigDecimal totalBet = bets.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> winningBets = new HashMap<>();
        BigDecimal totalPayout = BigDecimal.ZERO;

        // Check each bet
        for (Map.Entry<String, BigDecimal> bet : bets.entrySet()) {
            String betType = bet.getKey();
            BigDecimal betAmount = bet.getValue();

            if (isBetWinner(betType, winningNumber, type)) {
                BigDecimal payout = betAmount.multiply(getPayoutMultiplier(betType));
                winningBets.put(betType, payout);
                totalPayout = totalPayout.add(payout);
            }
        }

        return RouletteResultData.builder()
            .winningNumber(winningNumber)
            .color(getColor(winningNumber))
            .isEven(winningNumber > 0 && winningNumber % 2 == 0)
            .isRed(RED_NUMBERS.contains(winningNumber))
            .isBlack(BLACK_NUMBERS.contains(winningNumber))
            .dozen(getDozen(winningNumber))
            .column(getColumn(winningNumber))
            .half(getHalf(winningNumber))
            .totalBet(totalBet)
            .totalPayout(totalPayout)
            .netProfit(totalPayout.subtract(totalBet))
            .bets(bets)
            .winningBets(winningBets)
            .rouletteType(type)
            .build();
    }

    /**
     * Check if a bet wins
     */
    private boolean isBetWinner(String betType, int winningNumber, RouletteType type) {
        // Straight up (single number)
        if (betType.matches("\\d+")) {
            return Integer.parseInt(betType) == winningNumber;
        }

        // Special case for 00 in American roulette
        if (betType.equals("00")) {
            return type == RouletteType.AMERICAN && winningNumber == 37;
        }

        return switch (betType.toLowerCase()) {
            // Colors
            case "red" -> RED_NUMBERS.contains(winningNumber);
            case "black" -> BLACK_NUMBERS.contains(winningNumber);

            // Even/Odd
            case "even" -> winningNumber > 0 && winningNumber % 2 == 0;
            case "odd" -> winningNumber > 0 && winningNumber % 2 == 1;

            // Halves
            case "low", "1-18" -> winningNumber >= 1 && winningNumber <= 18;
            case "high", "19-36" -> winningNumber >= 19 && winningNumber <= 36;

            // Dozens
            case "1st12", "dozen1" -> winningNumber >= 1 && winningNumber <= 12;
            case "2nd12", "dozen2" -> winningNumber >= 13 && winningNumber <= 24;
            case "3rd12", "dozen3" -> winningNumber >= 25 && winningNumber <= 36;

            // Columns
            case "col1", "column1" -> winningNumber > 0 && (winningNumber - 1) % 3 == 0;
            case "col2", "column2" -> winningNumber > 0 && (winningNumber - 1) % 3 == 1;
            case "col3", "column3" -> winningNumber > 0 && (winningNumber - 1) % 3 == 2;

            // Split (two numbers)
            case String s when s.contains("-") && s.split("-").length == 2 -> {
                String[] numbers = s.split("-");
                try {
                    int num1 = Integer.parseInt(numbers[0]);
                    int num2 = Integer.parseInt(numbers[1]);
                    yield winningNumber == num1 || winningNumber == num2;
                } catch (NumberFormatException e) {
                    yield false;
                }
            }

            // Street (three numbers in a row)
            case String s when s.startsWith("street") -> {
                try {
                    int firstNum = Integer.parseInt(s.replace("street", ""));
                    yield winningNumber >= firstNum && winningNumber <= firstNum + 2;
                } catch (NumberFormatException e) {
                    yield false;
                }
            }

            // Corner (four numbers)
            case String s when s.startsWith("corner") -> {
                // Format: corner1 (covers 1,2,4,5), corner2 (covers 2,3,5,6), etc.
                yield false; // Simplified, implement as needed
            }

            default -> false;
        };
    }

    /**
     * Get payout multiplier for bet type
     */
    private BigDecimal getPayoutMultiplier(String betType) {
        // Straight up (single number)
        if (betType.matches("\\d+") || betType.equals("00")) {
            return BigDecimal.valueOf(36); // 35:1 + original bet
        }

        return switch (betType.toLowerCase()) {
            // Even money bets (1:1)
            case "red", "black", "even", "odd", "low", "high", "1-18", "19-36" ->
                BigDecimal.valueOf(2);

            // Dozen and column bets (2:1)
            case "1st12", "2nd12", "3rd12", "dozen1", "dozen2", "dozen3",
                 "col1", "col2", "col3", "column1", "column2", "column3" ->
                BigDecimal.valueOf(3);

            // Split (17:1)
            case String s when s.contains("-") && s.split("-").length == 2 ->
                BigDecimal.valueOf(18);

            // Street (11:1)
            case String s when s.startsWith("street") ->
                BigDecimal.valueOf(12);

            // Corner (8:1)
            case String s when s.startsWith("corner") ->
                BigDecimal.valueOf(9);

            // Six line (5:1)
            case String s when s.startsWith("line") ->
                BigDecimal.valueOf(6);

            default -> BigDecimal.ZERO;
        };
    }

    private String getColor(int number) {
        if (number == 0 || number == 37) return "GREEN"; // 0 or 00
        if (RED_NUMBERS.contains(number)) return "RED";
        if (BLACK_NUMBERS.contains(number)) return "BLACK";
        return "GREEN";
    }

    private Integer getDozen(int number) {
        if (number >= 1 && number <= 12) return 1;
        if (number >= 13 && number <= 24) return 2;
        if (number >= 25 && number <= 36) return 3;
        return null;
    }

    private Integer getColumn(int number) {
        if (number == 0 || number == 37) return null;
        return ((number - 1) % 3) + 1;
    }

    private String getHalf(int number) {
        if (number >= 1 && number <= 18) return "LOW";
        if (number >= 19 && number <= 36) return "HIGH";
        return null;
    }

    /**
     * Get all valid bet types for UI
     */
    public static Map<String, String> getBetTypes() {
        Map<String, String> betTypes = new LinkedHashMap<>();

        // Single numbers
        for (int i = 0; i <= 36; i++) {
            betTypes.put(String.valueOf(i), "Straight (" + i + ")");
        }

        // Outside bets
        betTypes.put("red", "Red (1:1)");
        betTypes.put("black", "Black (1:1)");
        betTypes.put("even", "Even (1:1)");
        betTypes.put("odd", "Odd (1:1)");
        betTypes.put("1-18", "Low 1-18 (1:1)");
        betTypes.put("19-36", "High 19-36 (1:1)");

        // Dozens
        betTypes.put("1st12", "1st Dozen (2:1)");
        betTypes.put("2nd12", "2nd Dozen (2:1)");
        betTypes.put("3rd12", "3rd Dozen (2:1)");

        // Columns
        betTypes.put("col1", "Column 1 (2:1)");
        betTypes.put("col2", "Column 2 (2:1)");
        betTypes.put("col3", "Column 3 (2:1)");

        return betTypes;
    }

    /**
     * Get house edge for roulette type
     */
    public static double getHouseEdge(RouletteType type) {
        return type == RouletteType.EUROPEAN ? 2.70 : 5.26; // Percentage
    }

    public enum RouletteType {
        EUROPEAN,  // Single 0
        AMERICAN   // 0 and 00
    }
}
