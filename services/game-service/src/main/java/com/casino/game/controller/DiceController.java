package com.casino.game.controller;

import com.casino.game.dto.DiceGameResultData;
import com.casino.game.service.DiceGameEngine;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/games/dice")
@RequiredArgsConstructor
public class DiceController {

    private final DiceGameEngine diceGameEngine;

    @PostMapping("/roll")
    public ResponseEntity<DiceGameResultData> roll(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody RollDiceRequest request
    ) {
        log.info("POST /games/dice/roll - userId: {}, betAmount: {}, target: {}, rollOver: {}",
            userId, request.getBetAmount(), request.getTargetNumber(), request.isRollOver());

        DiceGameResultData result = diceGameEngine.roll(
            request.getServerSeed(),
            request.getClientSeed(),
            request.getNonce(),
            request.getBetAmount(),
            request.getTargetNumber(),
            request.isRollOver()
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/multiplier")
    public ResponseEntity<BigDecimal> calculateMultiplier(
        @RequestParam @Min(2) @Max(98) int targetNumber,
        @RequestParam boolean rollOver
    ) {
        log.info("GET /games/dice/multiplier - target: {}, rollOver: {}", targetNumber, rollOver);

        BigDecimal multiplier = diceGameEngine.calculateMultiplier(targetNumber, rollOver);

        return ResponseEntity.ok(multiplier);
    }

    @GetMapping("/info")
    public ResponseEntity<String> getInfo() {
        log.info("GET /games/dice/info");

        String info = """
            Dice Game Info:
            - Roll result: 0-99 (displayed as 0.00-99.99)
            - Target: 2-98
            - Choose to roll OVER or UNDER the target
            - Multiplier adjusts based on probability
            - House edge: 1%

            Example:
            - Roll OVER 50: 49% win chance, ~2x multiplier
            - Roll UNDER 25: 25% win chance, ~4x multiplier
            - Roll OVER 90: 9% win chance, ~10x multiplier
            """;

        return ResponseEntity.ok(info);
    }

    @Data
    public static class RollDiceRequest {
        @NotNull
        private BigDecimal betAmount;

        @NotNull
        @Min(2)
        @Max(98)
        private Integer targetNumber;

        @NotNull
        private Boolean rollOver; // true = roll over, false = roll under

        @NotNull
        private String serverSeed;

        @NotNull
        private String clientSeed;

        @NotNull
        private Long nonce;
    }
}
