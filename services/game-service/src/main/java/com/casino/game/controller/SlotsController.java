package com.casino.game.controller;

import com.casino.game.dto.SlotsResultData;
import com.casino.game.engine.SlotsGameEngine;
import com.casino.game.model.Game;
import com.casino.game.service.GameService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Slots Game Controller
 * REST endpoints for 5-reel slot machine
 */
@Slf4j
@RestController
@RequestMapping("/api/games/slots")
@RequiredArgsConstructor
public class SlotsController {

    private final SlotsGameEngine slotsGameEngine;
    private final GameService gameService;

    /**
     * POST /api/games/slots/spin
     * Spin the slot machine
     */
    @PostMapping("/spin")
    public ResponseEntity<Game> spin(@RequestHeader("Authorization") String token,
                                     @RequestBody SpinRequest request) {
        try {
            String userId = extractUserIdFromToken(token);

            // Validate bet amount
            if (request.getBetAmount().compareTo(BigDecimal.valueOf(0.10)) < 0) {
                return ResponseEntity.badRequest().build();
            }
            if (request.getBetAmount().compareTo(BigDecimal.valueOf(1000)) > 0) {
                return ResponseEntity.badRequest().build();
            }

            // Create game record
            Game game = gameService.createGame(
                userId,
                "SLOTS",
                request.getBetAmount(),
                request.getClientSeed()
            );

            // Play the game
            SlotsResultData result = slotsGameEngine.spin(
                game.getServerSeed(),
                game.getClientSeed(),
                game.getNonce(),
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

            log.info("Slots spin - User: {}, Bet: {}, Payout: {}, Win: {}",
                userId, request.getBetAmount(), result.getPayout(), result.getIsWin());

            return ResponseEntity.ok(game);

        } catch (Exception e) {
            log.error("Error spinning slots", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/games/slots/info
     * Get game information
     */
    @GetMapping("/info")
    public ResponseEntity<SlotsInfo> getInfo() {
        SlotsInfo info = new SlotsInfo();
        info.setGameName("Classic Slots");
        info.setReels(5);
        info.setRows(3);
        info.setPaylines(25);
        info.setMinBet(BigDecimal.valueOf(0.10));
        info.setMaxBet(BigDecimal.valueOf(1000));
        info.setRtp(BigDecimal.valueOf(0.95)); // 95%
        info.setHouseEdge(BigDecimal.valueOf(0.05)); // 5%
        info.setMaxWin(BigDecimal.valueOf(25000)); // Max multiplier * max bet

        return ResponseEntity.ok(info);
    }

    private String extractUserIdFromToken(String token) {
        // TODO: Implement JWT token extraction
        return "user-123";
    }

    @Data
    public static class SpinRequest {
        private BigDecimal betAmount;
        private String clientSeed;
    }

    @Data
    public static class SlotsInfo {
        private String gameName;
        private Integer reels;
        private Integer rows;
        private Integer paylines;
        private BigDecimal minBet;
        private BigDecimal maxBet;
        private BigDecimal rtp;
        private BigDecimal houseEdge;
        private BigDecimal maxWin;
    }
}
