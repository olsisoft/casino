package com.casino.game.controller;

import com.casino.game.dto.CoinFlipResultData;
import com.casino.game.service.CoinFlipEngine;
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
@RequestMapping("/games/coin-flip")
@RequiredArgsConstructor
public class CoinFlipController {

    private final CoinFlipEngine coinFlipEngine;

    @PostMapping("/flip")
    public ResponseEntity<CoinFlipResultData> flip(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody FlipCoinRequest request
    ) {
        log.info("POST /games/coin-flip/flip - userId: {}, betAmount: {}, choice: {}",
            userId, request.getBetAmount(), request.getPlayerChoice());

        CoinFlipResultData result = coinFlipEngine.flip(
            request.getServerSeed(),
            request.getClientSeed(),
            request.getNonce(),
            request.getBetAmount(),
            request.getPlayerChoice()
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping("/verify")
    public ResponseEntity<VerificationResponse> verifyResult(
        @Valid @RequestBody VerifyCoinFlipRequest request
    ) {
        log.info("POST /games/coin-flip/verify - nonce: {}", request.getNonce());

        boolean isValid = coinFlipEngine.verifyResult(
            request.getServerSeed(),
            request.getClientSeed(),
            request.getNonce(),
            request.getExpectedResult()
        );

        return ResponseEntity.ok(new VerificationResponse(isValid));
    }

    @GetMapping("/multiplier")
    public ResponseEntity<BigDecimal> getMultiplier() {
        log.info("GET /games/coin-flip/multiplier");

        BigDecimal multiplier = coinFlipEngine.getMultiplier();

        return ResponseEntity.ok(multiplier);
    }

    @GetMapping("/info")
    public ResponseEntity<String> getInfo() {
        log.info("GET /games/coin-flip/info");

        String info = """
            Coin Flip Game Info:
            - Simple 50/50 game
            - Choose HEADS or TAILS
            - Win chance: 50%
            - Multiplier: ~1.98x (with 1% house edge)
            - Provably fair
            """;

        return ResponseEntity.ok(info);
    }

    @Data
    public static class FlipCoinRequest {
        @NotNull
        private BigDecimal betAmount;

        @NotNull
        private CoinFlipEngine.CoinSide playerChoice; // HEADS or TAILS

        @NotNull
        private String serverSeed;

        @NotNull
        private String clientSeed;

        @NotNull
        private Long nonce;
    }

    @Data
    public static class VerifyCoinFlipRequest {
        @NotNull
        private String serverSeed;

        @NotNull
        private String clientSeed;

        @NotNull
        private Long nonce;

        @NotNull
        private String expectedResult; // "HEADS" or "TAILS"
    }

    @Data
    public static class VerificationResponse {
        private boolean valid;

        public VerificationResponse(boolean valid) {
            this.valid = valid;
        }
    }
}
