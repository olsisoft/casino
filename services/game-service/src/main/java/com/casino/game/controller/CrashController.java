package com.casino.game.controller;

import com.casino.game.dto.CrashGameResultData;
import com.casino.game.service.CrashGameEngine;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/games/crash")
@RequiredArgsConstructor
public class CrashController {

    private final CrashGameEngine crashGameEngine;

    @PostMapping("/play")
    public ResponseEntity<CrashGameResultData> play(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody PlayCrashRequest request
    ) {
        log.info("POST /games/crash/play - userId: {}, betAmount: {}, autoCashout: {}",
            userId, request.getBetAmount(), request.getAutoCashoutAt());

        CrashGameResultData result = crashGameEngine.play(
            request.getServerSeed(),
            request.getClientSeed(),
            request.getNonce(),
            request.getBetAmount(),
            request.getAutoCashoutAt()
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping("/cashout")
    public ResponseEntity<CrashGameResultData> cashout(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody CashoutCrashRequest request
    ) {
        log.info("POST /games/crash/cashout - userId: {}, cashoutAt: {}",
            userId, request.getCashoutMultiplier());

        CrashGameResultData result = crashGameEngine.cashout(
            request.getCurrentGame(),
            request.getCashoutMultiplier()
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping("/verify")
    public ResponseEntity<VerificationResponse> verifyResult(
        @Valid @RequestBody VerifyCrashRequest request
    ) {
        log.info("POST /games/crash/verify - nonce: {}", request.getNonce());

        boolean isValid = crashGameEngine.verifyResult(
            request.getServerSeed(),
            request.getClientSeed(),
            request.getNonce(),
            request.getExpectedCrashPoint()
        );

        return ResponseEntity.ok(new VerificationResponse(isValid));
    }

    @GetMapping("/statistics")
    public ResponseEntity<CrashGameEngine.CrashStatistics> getStatistics() {
        log.info("GET /games/crash/statistics");

        CrashGameEngine.CrashStatistics stats = crashGameEngine.getCrashStatistics();

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/rtp")
    public ResponseEntity<BigDecimal> getRTP() {
        log.info("GET /games/crash/rtp");

        BigDecimal rtp = crashGameEngine.calculateRTP();

        return ResponseEntity.ok(rtp);
    }

    @Data
    public static class PlayCrashRequest {
        @NotNull
        private BigDecimal betAmount;

        @DecimalMin("1.00")
        private BigDecimal autoCashoutAt; // Optional

        @NotNull
        private String serverSeed;

        @NotNull
        private String clientSeed;

        @NotNull
        private Long nonce;
    }

    @Data
    public static class CashoutCrashRequest {
        @NotNull
        private CrashGameResultData currentGame;

        @NotNull
        @DecimalMin("1.00")
        private BigDecimal cashoutMultiplier;
    }

    @Data
    public static class VerifyCrashRequest {
        @NotNull
        private String serverSeed;

        @NotNull
        private String clientSeed;

        @NotNull
        private Long nonce;

        @NotNull
        private BigDecimal expectedCrashPoint;
    }

    @Data
    public static class VerificationResponse {
        private boolean valid;

        public VerificationResponse(boolean valid) {
            this.valid = valid;
        }
    }
}
