package com.casino.game.controller;

import com.casino.game.dto.PokerResultData;
import com.casino.game.engine.PokerGameEngine;
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
 * Texas Hold'em Poker Game Controller
 * REST endpoints for heads-up poker vs dealer
 */
@Slf4j
@RestController
@RequestMapping("/api/games/poker")
@RequiredArgsConstructor
public class PokerController {

    private final PokerGameEngine pokerGameEngine;
    private final GameService gameService;

    /**
     * POST /api/games/poker/play
     * Play a hand of Texas Hold'em
     */
    @PostMapping("/play")
    public ResponseEntity<Game> play(@RequestHeader("Authorization") String token,
                                     @RequestBody PlayRequest request) {
        try {
            String userId = extractUserIdFromToken(token);

            // Validate ante bet
            if (request.getAnteBet().compareTo(BigDecimal.valueOf(1)) < 0) {
                return ResponseEntity.badRequest().build();
            }
            if (request.getAnteBet().compareTo(BigDecimal.valueOf(5000)) > 0) {
                return ResponseEntity.badRequest().build();
            }

            // Create game record
            Game game = gameService.createGame(
                userId,
                "POKER",
                request.getAnteBet(),
                request.getClientSeed()
            );

            // Play the game
            PokerResultData result = pokerGameEngine.play(
                game.getServerSeed(),
                game.getClientSeed(),
                game.getNonce(),
                request.getAnteBet()
            );

            // Update game with result
            game = gameService.completeGame(
                game.getId(),
                result.getPayout(),
                result.getProfit(),
                result.getIsWin(),
                result
            );

            log.info("Poker - User: {}, Ante: {}, Player: {}, Dealer: {}, Result: {}, Payout: {}",
                userId, request.getAnteBet(), result.getPlayerHandRank(),
                result.getDealerHandRank(), result.getResult(), result.getPayout());

            return ResponseEntity.ok(game);

        } catch (Exception e) {
            log.error("Error playing poker", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/games/poker/info
     * Get game information
     */
    @GetMapping("/info")
    public ResponseEntity<PokerInfo> getInfo() {
        PokerInfo info = new PokerInfo();
        info.setGameName("Texas Hold'em Poker");
        info.setVariant("Heads-Up vs Dealer");
        info.setMinBet(BigDecimal.valueOf(1));
        info.setMaxBet(BigDecimal.valueOf(5000));
        info.setRtp(BigDecimal.valueOf(0.98)); // 98%
        info.setHouseEdge(BigDecimal.valueOf(0.02)); // 2%

        return ResponseEntity.ok(info);
    }

    /**
     * GET /api/games/poker/paytable
     * Get payout table
     */
    @GetMapping("/paytable")
    public ResponseEntity<Map<String, Integer>> getPayTable() {
        Map<String, Integer> payTable = new HashMap<>();
        payTable.put("ROYAL_FLUSH", 100);
        payTable.put("STRAIGHT_FLUSH", 50);
        payTable.put("FOUR_OF_A_KIND", 20);
        payTable.put("FULL_HOUSE", 8);
        payTable.put("FLUSH", 5);
        payTable.put("STRAIGHT", 4);
        payTable.put("THREE_OF_A_KIND", 3);
        payTable.put("TWO_PAIR", 2);
        payTable.put("PAIR", 1);

        return ResponseEntity.ok(payTable);
    }

    /**
     * GET /api/games/poker/rules
     * Get game rules
     */
    @GetMapping("/rules")
    public ResponseEntity<PokerRules> getRules() {
        PokerRules rules = new PokerRules();
        rules.setDescription("Heads-up Texas Hold'em against the dealer");
        rules.addRule("Player and dealer each receive 2 hole cards");
        rules.addRule("5 community cards are dealt (flop, turn, river)");
        rules.addRule("Best 5-card hand wins");
        rules.addRule("Payouts based on winning hand strength");
        rules.addRule("Tie returns ante bet");

        return ResponseEntity.ok(rules);
    }

    private String extractUserIdFromToken(String token) {
        // TODO: Implement JWT token extraction
        return "user-123";
    }

    @Data
    public static class PlayRequest {
        private BigDecimal anteBet;
        private String clientSeed;
    }

    @Data
    public static class PokerInfo {
        private String gameName;
        private String variant;
        private BigDecimal minBet;
        private BigDecimal maxBet;
        private BigDecimal rtp;
        private BigDecimal houseEdge;
    }

    @Data
    public static class PokerRules {
        private String description;
        private java.util.List<String> rules = new java.util.ArrayList<>();

        public void addRule(String rule) {
            rules.add(rule);
        }
    }
}
