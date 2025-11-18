package com.casino.game.dto;

import com.casino.game.service.VideoPokerEngine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoPokerResultData {
    private List<String> hand; // 5 cards
    private boolean[] heldCards; // which cards to hold
    private VideoPokerEngine.HandRank handRank;
    private BigDecimal betAmount;
    private BigDecimal payout;
    private String gameState; // INITIAL_DEAL, COMPLETE

    // For continuing the game (not sent to client)
    private List<VideoPokerEngine.Card> deck;
}
