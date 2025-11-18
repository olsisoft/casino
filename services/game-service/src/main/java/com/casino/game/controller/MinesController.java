package com.casino.game.controller;

import com.casino.game.dto.MinesGameResultData;
import com.casino.game.service.MinesGameEngine;
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
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/games/mines")
@RequiredArgsConstructor
public class MinesController {

    private final MinesGameEngine minesGameEngine;

    @PostMapping("/start")
    public ResponseEntity<MinesGameResultData> startGame(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody StartMinesRequest request
    ) {
        log.info("POST /games/mines/start - userId: {}, betAmount: {}, mines: {}",
            userId, request.getBetAmount(), request.getNumberOfMines());

        MinesGameResultData result = minesGameEngine.startGame(
            request.getServerSeed(),
            request.getClientSeed(),
            request.getNonce(),
            request.getBetAmount(),
            request.getNumberOfMines()
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping("/reveal")
    public ResponseEntity<MinesGameResultData> revealTile(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody RevealTileRequest request
    ) {
        log.info("POST /games/mines/reveal - userId: {}, position: {}", userId, request.getPosition());

        MinesGameResultData result = minesGameEngine.revealTile(
            request.getCurrentGame(),
            request.getPosition()
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping("/cashout")
    public ResponseEntity<MinesGameResultData> cashout(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody CashoutRequest request
    ) {
        log.info("POST /games/mines/cashout - userId: {}", userId);

        MinesGameResultData result = minesGameEngine.cashout(request.getCurrentGame());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/multipliers/{numberOfMines}")
    public ResponseEntity<Map<Integer, BigDecimal>> getMultiplierTable(
        @PathVariable @Min(1) @Max(24) int numberOfMines
    ) {
        log.info("GET /games/mines/multipliers/{}", numberOfMines);

        Map<Integer, BigDecimal> table = minesGameEngine.getMultiplierTable(numberOfMines);

        return ResponseEntity.ok(table);
    }

    @Data
    public static class StartMinesRequest {
        @NotNull
        private BigDecimal betAmount;

        @NotNull
        @Min(1)
        @Max(24)
        private Integer numberOfMines;

        @NotNull
        private String serverSeed;

        @NotNull
        private String clientSeed;

        @NotNull
        private Long nonce;
    }

    @Data
    public static class RevealTileRequest {
        @NotNull
        private MinesGameResultData currentGame;

        @NotNull
        @Min(0)
        @Max(24)
        private Integer position;
    }

    @Data
    public static class CashoutRequest {
        @NotNull
        private MinesGameResultData currentGame;
    }
}
