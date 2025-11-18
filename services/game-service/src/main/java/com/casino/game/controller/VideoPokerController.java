package com.casino.game.controller;

import com.casino.game.dto.VideoPokerResultData;
import com.casino.game.service.VideoPokerEngine;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/games/video-poker")
@RequiredArgsConstructor
public class VideoPokerController {

    private final VideoPokerEngine videoPokerEngine;

    @PostMapping("/deal")
    public ResponseEntity<VideoPokerResultData> dealInitialHand(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody DealPokerRequest request
    ) {
        log.info("POST /games/video-poker/deal - userId: {}, betAmount: {}",
            userId, request.getBetAmount());

        VideoPokerResultData result = videoPokerEngine.dealInitialHand(
            request.getServerSeed(),
            request.getClientSeed(),
            request.getNonce(),
            request.getBetAmount()
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping("/draw")
    public ResponseEntity<VideoPokerResultData> drawCards(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody DrawCardsRequest request
    ) {
        log.info("POST /games/video-poker/draw - userId: {}", userId);

        VideoPokerResultData result = videoPokerEngine.drawCards(
            request.getCurrentGame(),
            request.getHeldCards()
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/paytable")
    public ResponseEntity<String> getPaytable() {
        log.info("GET /games/video-poker/paytable");

        String paytable = """
            Jacks or Better Paytable:
            - Royal Flush: 800x
            - Straight Flush: 50x
            - Four of a Kind: 25x
            - Full House: 9x
            - Flush: 6x
            - Straight: 4x
            - Three of a Kind: 3x
            - Two Pair: 2x
            - Jacks or Better: 1x
            """;

        return ResponseEntity.ok(paytable);
    }

    @Data
    public static class DealPokerRequest {
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
    public static class DrawCardsRequest {
        @NotNull
        private VideoPokerResultData currentGame;

        @NotNull
        @Size(min = 5, max = 5)
        private boolean[] heldCards; // Array of 5 booleans indicating which cards to hold
    }
}
