package com.casino.game.controller;

import com.casino.game.dto.*;
import com.casino.game.entity.GameConfig;
import com.casino.game.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @GetMapping
    public ResponseEntity<List<GameConfigDto>> getAllGames() {
        log.info("GET /games - Fetching all available games");
        List<GameConfigDto> games = gameService.getAllGames();
        return ResponseEntity.ok(games);
    }

    @GetMapping("/type/{gameType}")
    public ResponseEntity<List<GameConfigDto>> getGamesByType(@PathVariable GameConfig.GameType gameType) {
        log.info("GET /games/type/{} - Fetching games by type", gameType);
        List<GameConfigDto> games = gameService.getGamesByType(gameType);
        return ResponseEntity.ok(games);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<GameConfigDto>> getMostPopularGames() {
        log.info("GET /games/popular - Fetching most popular games");
        List<GameConfigDto> games = gameService.getMostPopularGames();
        return ResponseEntity.ok(games);
    }

    @GetMapping("/{gameCode}")
    public ResponseEntity<GameConfigDto> getGame(@PathVariable String gameCode) {
        log.info("GET /games/{} - Fetching game details", gameCode);
        GameConfigDto game = gameService.getGame(gameCode);
        return ResponseEntity.ok(game);
    }

    @PostMapping("/sessions/start")
    public ResponseEntity<GameSessionDto> startSession(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody StartSessionRequest request
    ) {
        log.info("POST /games/sessions/start - userId: {}, gameCode: {}", userId, request.getGameCode());
        GameSessionDto session = gameService.startSession(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    @GetMapping("/sessions/current")
    public ResponseEntity<GameSessionDto> getCurrentSession(@RequestHeader("X-User-Id") String userId) {
        log.info("GET /games/sessions/current - userId: {}", userId);
        GameSessionDto session = gameService.getCurrentSession(userId);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/sessions/history")
    public ResponseEntity<List<GameSessionDto>> getSessionHistory(@RequestHeader("X-User-Id") String userId) {
        log.info("GET /games/sessions/history - userId: {}", userId);
        List<GameSessionDto> sessions = gameService.getSessionHistory(userId);
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/sessions/{sessionId}/end")
    public ResponseEntity<Void> endSession(
        @PathVariable String sessionId,
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("POST /games/sessions/{}/end - userId: {}", sessionId, userId);
        gameService.endSession(sessionId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/play")
    public ResponseEntity<PlayRoundResponse> playRound(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody PlayRoundRequest request
    ) {
        log.info("POST /games/play - userId: {}, sessionId: {}, betAmount: {}",
            userId, request.getSessionId(), request.getBetAmount());

        PlayRoundResponse response = gameService.playRound(userId, request);

        log.info("Round completed - outcome: {}, winAmount: {}, multiplier: {}",
            response.getOutcome(), response.getWinAmount(), response.getMultiplier());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Game Service is running");
    }
}
