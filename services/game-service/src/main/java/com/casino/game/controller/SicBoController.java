package com.casino.game.controller;

import com.casino.game.dto.SicBoResultData;
import com.casino.game.engine.SicBoGameEngine;
import com.casino.game.model.Game;
import com.casino.game.service.GameService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Sic Bo Game Controller
 * REST endpoints for Chinese dice game
 */
@Slf4j
@RestController
@RequestMapping("/api/games/sicbo")
@RequiredArgsConstructor
public class SicBoController {

    private final SicBoGameEngine sicBoGameEngine;
    private final GameService gameService;

    /**
     * POST /api/games/sicbo/play
     * Play a round of Sic Bo
     */
    @PostMapping("/play")
    public ResponseEntity<Game> play(@RequestHeader("Authorization") String token,
                                     @RequestBody PlayRequest request) {
        try {
            String userId = extractUserIdFromToken(token);

            // Validate bets
            if (request.getBets() == null || request.getBets().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Validate total bet amount
            BigDecimal totalBet = request.getBets().values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalBet.compareTo(BigDecimal.valueOf(0.10)) < 0) {
                return ResponseEntity.badRequest().build();
            }
            if (totalBet.compareTo(BigDecimal.valueOf(1000)) > 0) {
                return ResponseEntity.badRequest().build();
            }

            // Create game record
            Game game = gameService.createGame(
                userId,
                "SICBO",
                totalBet,
                request.getClientSeed()
            );

            // Play the game
            SicBoResultData result = sicBoGameEngine.play(
                game.getServerSeed(),
                game.getClientSeed(),
                game.getNonce(),
                request.getBets()
            );

            // Update game with result
            game = gameService.completeGame(
                game.getId(),
                result.getTotalPayout(),
                result.getProfit(),
                result.getIsWin(),
                result
            );

            log.info("Sic Bo - User: {}, Dice: {}, Total: {}, Bets: {}, Payout: {}",
                userId, result.getDice(), result.getTotal(),
                request.getBets().size(), result.getTotalPayout());

            return ResponseEntity.ok(game);

        } catch (Exception e) {
            log.error("Error playing sic bo", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/games/sicbo/info
     * Get game information
     */
    @GetMapping("/info")
    public ResponseEntity<SicBoInfo> getInfo() {
        SicBoInfo info = new SicBoInfo();
        info.setGameName("Sic Bo");
        info.setDescription("Traditional Chinese dice game");
        info.setDiceCount(3);
        info.setMinBet(BigDecimal.valueOf(0.10));
        info.setMaxBet(BigDecimal.valueOf(1000));
        info.setAverageRtp(BigDecimal.valueOf(0.972)); // 97.2%
        info.setMaxPayout(180); // Specific triple

        return ResponseEntity.ok(info);
    }

    /**
     * GET /api/games/sicbo/bet-types
     * Get all available bet types and their payouts
     */
    @GetMapping("/bet-types")
    public ResponseEntity<Map<String, BetTypeInfo>> getBetTypes() {
        Map<String, BetTypeInfo> betTypes = new HashMap<>();

        // Big/Small
        betTypes.put("SMALL", new BetTypeInfo("Total 4-10", "1:1", 1.96));
        betTypes.put("BIG", new BetTypeInfo("Total 11-17", "1:1", 1.96));

        // Totals
        betTypes.put("TOTAL_4", new BetTypeInfo("Total = 4", "60:1", 61.11));
        betTypes.put("TOTAL_17", new BetTypeInfo("Total = 17", "60:1", 61.11));
        betTypes.put("TOTAL_5", new BetTypeInfo("Total = 5", "30:1", 31.94));
        betTypes.put("TOTAL_16", new BetTypeInfo("Total = 16", "30:1", 31.94));
        betTypes.put("TOTAL_6", new BetTypeInfo("Total = 6", "17:1", 18.98));
        betTypes.put("TOTAL_15", new BetTypeInfo("Total = 15", "17:1", 18.98));
        betTypes.put("TOTAL_7", new BetTypeInfo("Total = 7", "12:1", 13.19));
        betTypes.put("TOTAL_14", new BetTypeInfo("Total = 14", "12:1", 13.19));
        betTypes.put("TOTAL_8", new BetTypeInfo("Total = 8", "8:1", 9.03));
        betTypes.put("TOTAL_13", new BetTypeInfo("Total = 13", "8:1", 9.03));

        // Singles
        betTypes.put("SINGLE_1", new BetTypeInfo("At least one 1", "1:1, 2:1, 3:1", 1.0));
        betTypes.put("SINGLE_6", new BetTypeInfo("At least one 6", "1:1, 2:1, 3:1", 1.0));

        // Doubles
        betTypes.put("DOUBLE_1", new BetTypeInfo("At least two 1s", "10:1", 11.11));
        betTypes.put("DOUBLE_6", new BetTypeInfo("At least two 6s", "10:1", 11.11));

        // Triples
        betTypes.put("ANY_TRIPLE", new BetTypeInfo("Any triple", "30:1", 31.94));
        betTypes.put("TRIPLE_1", new BetTypeInfo("Three 1s", "180:1", 181.0));
        betTypes.put("TRIPLE_6", new BetTypeInfo("Three 6s", "180:1", 181.0));

        // Combos
        betTypes.put("COMBO_1_2", new BetTypeInfo("1 and 2 appear", "6:1", 7.14));
        betTypes.put("COMBO_5_6", new BetTypeInfo("5 and 6 appear", "6:1", 7.14));

        return ResponseEntity.ok(betTypes);
    }

    /**
     * GET /api/games/sicbo/rules
     * Get game rules
     */
    @GetMapping("/rules")
    public ResponseEntity<SicBoRules> getRules() {
        SicBoRules rules = new SicBoRules();
        rules.setDescription("Bet on the outcome of three dice");
        rules.addRule("Place bets on various outcomes");
        rules.addRule("Three dice are rolled");
        rules.addRule("Winning bets are paid according to payout table");
        rules.addRule("Multiple bets can be placed per round");
        rules.addRule("Small/Big bets lose on any triple");

        return ResponseEntity.ok(rules);
    }

    private String extractUserIdFromToken(String token) {
        // TODO: Implement JWT token extraction
        return "user-123";
    }

    @Data
    public static class PlayRequest {
        private Map<SicBoGameEngine.BetType, BigDecimal> bets;
        private String clientSeed;
    }

    @Data
    public static class SicBoInfo {
        private String gameName;
        private String description;
        private Integer diceCount;
        private BigDecimal minBet;
        private BigDecimal maxBet;
        private BigDecimal averageRtp;
        private Integer maxPayout;
    }

    @Data
    @lombok.AllArgsConstructor
    public static class BetTypeInfo {
        private String description;
        private String payout;
        private Double multiplier;
    }

    @Data
    public static class SicBoRules {
        private String description;
        private java.util.List<String> rules = new java.util.ArrayList<>();

        public void addRule(String rule) {
            rules.add(rule);
        }
    }
}
