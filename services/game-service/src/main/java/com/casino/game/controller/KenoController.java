package com.casino.game.controller;

import com.casino.game.dto.KenoResultData;
import com.casino.game.engine.KenoGameEngine;
import com.casino.game.model.Game;
import com.casino.game.service.GameService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Keno Game Controller
 * REST endpoints for Keno lottery game
 */
@Slf4j
@RestController
@RequestMapping("/api/games/keno")
@RequiredArgsConstructor
public class KenoController {

    private final KenoGameEngine kenoGameEngine;
    private final GameService gameService;

    /**
     * POST /api/games/keno/play
     * Play a round of Keno
     */
    @PostMapping("/play")
    public ResponseEntity<Game> play(@RequestHeader("Authorization") String token,
                                     @RequestBody PlayRequest request) {
        try {
            String userId = extractUserIdFromToken(token);

            // Validate bet amount
            if (request.getBetAmount().compareTo(BigDecimal.valueOf(0.10)) < 0) {
                return ResponseEntity.badRequest().build();
            }
            if (request.getBetAmount().compareTo(BigDecimal.valueOf(100)) > 0) {
                return ResponseEntity.badRequest().build();
            }

            // Validate picks
            if (request.getPickedNumbers() == null || request.getPickedNumbers().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (request.getPickedNumbers().size() > 10) {
                return ResponseEntity.badRequest().build();
            }

            // Create game record
            Game game = gameService.createGame(
                userId,
                "KENO",
                request.getBetAmount(),
                request.getClientSeed()
            );

            // Play the game
            KenoResultData result = kenoGameEngine.play(
                game.getServerSeed(),
                game.getClientSeed(),
                game.getNonce(),
                request.getPickedNumbers(),
                request.getBetAmount()
            );

            // Update game with result
            game = gameService.completeGame(
                game.getId(),
                result.getPayout(),
                result.getProfit(),
                result.getIsWin(),
                result
            );

            log.info("Keno - User: {}, Picked: {}, Matches: {}, Multiplier: {}x, Payout: {}",
                userId, request.getPickedNumbers().size(), result.getMatchCount(),
                result.getMultiplier(), result.getPayout());

            return ResponseEntity.ok(game);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid keno request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error playing keno", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/games/keno/info
     * Get game information
     */
    @GetMapping("/info")
    public ResponseEntity<KenoInfo> getInfo() {
        KenoInfo info = new KenoInfo();
        info.setGameName("Keno");
        info.setMinPicks(1);
        info.setMaxPicks(10);
        info.setTotalNumbers(80);
        info.setDrawCount(20);
        info.setMinBet(BigDecimal.valueOf(0.10));
        info.setMaxBet(BigDecimal.valueOf(100));
        info.setMaxMultiplier(25000); // Pick 10, hit all 10
        info.setAverageRtp(BigDecimal.valueOf(0.75)); // 75%

        return ResponseEntity.ok(info);
    }

    /**
     * GET /api/games/keno/paytable
     * Get payout table for all pick counts
     */
    @GetMapping("/paytable")
    public ResponseEntity<PayTable> getPayTable() {
        PayTable payTable = new PayTable();

        // Simplified - showing key payouts for each pick count
        payTable.addPayout(1, 1, 3);
        payTable.addPayout(2, 2, 15);
        payTable.addPayout(3, 2, 2);
        payTable.addPayout(3, 3, 50);
        payTable.addPayout(4, 2, 1);
        payTable.addPayout(4, 3, 5);
        payTable.addPayout(4, 4, 100);
        payTable.addPayout(5, 3, 3);
        payTable.addPayout(5, 4, 15);
        payTable.addPayout(5, 5, 500);
        payTable.addPayout(10, 5, 2);
        payTable.addPayout(10, 6, 10);
        payTable.addPayout(10, 7, 50);
        payTable.addPayout(10, 8, 500);
        payTable.addPayout(10, 9, 5000);
        payTable.addPayout(10, 10, 25000);

        return ResponseEntity.ok(payTable);
    }

    private String extractUserIdFromToken(String token) {
        // TODO: Implement JWT token extraction
        return "user-123";
    }

    @Data
    public static class PlayRequest {
        private List<Integer> pickedNumbers;
        private BigDecimal betAmount;
        private String clientSeed;
    }

    @Data
    public static class KenoInfo {
        private String gameName;
        private Integer minPicks;
        private Integer maxPicks;
        private Integer totalNumbers;
        private Integer drawCount;
        private BigDecimal minBet;
        private BigDecimal maxBet;
        private Integer maxMultiplier;
        private BigDecimal averageRtp;
    }

    @Data
    public static class PayTable {
        private List<PayoutEntry> payouts = new java.util.ArrayList<>();

        public void addPayout(int picks, int matches, int multiplier) {
            payouts.add(new PayoutEntry(picks, matches, multiplier));
        }
    }

    @Data
    @lombok.AllArgsConstructor
    public static class PayoutEntry {
        private Integer picks;
        private Integer matches;
        private Integer multiplier;
    }
}
