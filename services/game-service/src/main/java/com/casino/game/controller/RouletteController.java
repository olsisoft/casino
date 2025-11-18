package com.casino.game.controller;

import com.casino.game.dto.RouletteResultData;
import com.casino.game.service.RouletteEngine;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/games/roulette")
@RequiredArgsConstructor
public class RouletteController {

    private final RouletteEngine rouletteEngine;

    @PostMapping("/spin")
    public ResponseEntity<RouletteResultData> spin(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody SpinRouletteRequest request
    ) {
        log.info("POST /games/roulette/spin - userId: {}, betAmount: {}, betType: {}, rouletteType: {}",
            userId, request.getBetAmount(), request.getBetType(), request.getRouletteType());

        RouletteResultData result = rouletteEngine.spin(
            request.getServerSeed(),
            request.getClientSeed(),
            request.getNonce(),
            request.getBetAmount(),
            request.getBetType(),
            request.getRouletteType()
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/bet-types")
    public ResponseEntity<String> getBetTypes() {
        log.info("GET /games/roulette/bet-types");

        String betTypes = """
            Available Bet Types:
            - Straight Up: Any number (0-36, 00 for American)
            - Red/Black: "red", "black"
            - Even/Odd: "even", "odd"
            - High/Low: "high" (19-36), "low" (1-18)
            - Dozens: "1st12", "2nd12", "3rd12"
            - Columns: "col1", "col2", "col3"

            Payouts:
            - Straight Up: 35:1
            - Red/Black, Even/Odd, High/Low: 1:1
            - Dozens, Columns: 2:1
            """;

        return ResponseEntity.ok(betTypes);
    }

    @Data
    public static class SpinRouletteRequest {
        @NotNull
        private BigDecimal betAmount;

        @NotBlank
        private String betType; // e.g., "red", "black", "17", "1st12"

        @NotNull
        private RouletteEngine.RouletteType rouletteType; // EUROPEAN or AMERICAN

        @NotNull
        private String serverSeed;

        @NotNull
        private String clientSeed;

        @NotNull
        private Long nonce;
    }
}
