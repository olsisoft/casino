package com.casino.game.controller;

import com.casino.game.dto.BlackjackResultData;
import com.casino.game.service.BlackjackEngine;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/games/blackjack")
@RequiredArgsConstructor
public class BlackjackController {

    private final BlackjackEngine blackjackEngine;

    @PostMapping("/start")
    public ResponseEntity<BlackjackResultData> startGame(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody StartBlackjackRequest request
    ) {
        log.info("POST /games/blackjack/start - userId: {}, betAmount: {}", userId, request.getBetAmount());

        BlackjackResultData result = blackjackEngine.startGame(
            request.getServerSeed(),
            request.getClientSeed(),
            request.getNonce(),
            request.getBetAmount()
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping("/hit")
    public ResponseEntity<BlackjackResultData> hit(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody BlackjackActionRequest request
    ) {
        log.info("POST /games/blackjack/hit - userId: {}", userId);

        BlackjackResultData result = blackjackEngine.hit(request.getCurrentGame());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/stand")
    public ResponseEntity<BlackjackResultData> stand(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody BlackjackActionRequest request
    ) {
        log.info("POST /games/blackjack/stand - userId: {}", userId);

        BlackjackResultData result = blackjackEngine.stand(request.getCurrentGame());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/double")
    public ResponseEntity<BlackjackResultData> doubleDown(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody BlackjackActionRequest request
    ) {
        log.info("POST /games/blackjack/double - userId: {}", userId);

        BlackjackResultData result = blackjackEngine.doubleDown(request.getCurrentGame());

        return ResponseEntity.ok(result);
    }

    @Data
    public static class StartBlackjackRequest {
        @NotNull
        private BigDecimal betAmount;

        @NotNull
        private String serverSeed;

        @NotNull
        private String clientSeed;

        @NotNull
        private Long nonce;
    }

    @Data
    public static class BlackjackActionRequest {
        @NotNull
        private BlackjackResultData currentGame;
    }
}
