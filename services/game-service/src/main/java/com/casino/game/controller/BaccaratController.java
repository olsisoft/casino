package com.casino.game.controller;

import com.casino.game.dto.BaccaratResultData;
import com.casino.game.engine.BaccaratGameEngine;
import com.casino.game.model.Game;
import com.casino.game.service.GameService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Baccarat Game Controller
 * REST endpoints for Baccarat card game
 */
@Slf4j
@RestController
@RequestMapping("/api/games/baccarat")
@RequiredArgsConstructor
public class BaccaratController {

    private final BaccaratGameEngine baccaratGameEngine;
    private final GameService gameService;

    /**
     * POST /api/games/baccarat/play
     * Play a round of Baccarat
     */
    @PostMapping("/play")
    public ResponseEntity<Game> play(@RequestHeader("Authorization") String token,
                                     @RequestBody PlayRequest request) {
        try {
            String userId = extractUserIdFromToken(token);

            // Validate bet amount
            if (request.getBetAmount().compareTo(BigDecimal.valueOf(1)) < 0) {
                return ResponseEntity.badRequest().build();
            }
            if (request.getBetAmount().compareTo(BigDecimal.valueOf(10000)) > 0) {
                return ResponseEntity.badRequest().build();
            }

            // Create game record
            Game game = gameService.createGame(
                userId,
                "BACCARAT",
                request.getBetAmount(),
                request.getClientSeed()
            );

            // Play the game
            BaccaratResultData result = baccaratGameEngine.play(
                game.getServerSeed(),
                game.getClientSeed(),
                game.getNonce(),
                request.getBetType(),
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

            log.info("Baccarat - User: {}, Bet: {} on {}, Winner: {}, Payout: {}",
                userId, request.getBetAmount(), request.getBetType(),
                result.getWinner(), result.getPayout());

            return ResponseEntity.ok(game);

        } catch (Exception e) {
            log.error("Error playing baccarat", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/games/baccarat/info
     * Get game information
     */
    @GetMapping("/info")
    public ResponseEntity<BaccaratInfo> getInfo() {
        BaccaratInfo info = new BaccaratInfo();
        info.setGameName("Baccarat");
        info.setMinBet(BigDecimal.valueOf(1));
        info.setMaxBet(BigDecimal.valueOf(10000));
        info.setPlayerRtp(BigDecimal.valueOf(0.9876)); // 98.76%
        info.setBankerRtp(BigDecimal.valueOf(0.9894)); // 98.94%
        info.setTieRtp(BigDecimal.valueOf(0.8564));    // 85.64%
        info.setPlayerPayout("1:1");
        info.setBankerPayout("0.95:1 (5% commission)");
        info.setTiePayout("8:1");

        return ResponseEntity.ok(info);
    }

    private String extractUserIdFromToken(String token) {
        // TODO: Implement JWT token extraction
        return "user-123";
    }

    @Data
    public static class PlayRequest {
        private BaccaratGameEngine.BetType betType;
        private BigDecimal betAmount;
        private String clientSeed;
    }

    @Data
    public static class BaccaratInfo {
        private String gameName;
        private BigDecimal minBet;
        private BigDecimal maxBet;
        private BigDecimal playerRtp;
        private BigDecimal bankerRtp;
        private BigDecimal tieRtp;
        private String playerPayout;
        private String bankerPayout;
        private String tiePayout;
    }
}
