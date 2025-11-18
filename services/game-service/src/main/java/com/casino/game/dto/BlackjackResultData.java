package com.casino.game.dto;

import com.casino.game.service.BlackjackEngine;
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
public class BlackjackResultData {
    private List<String> playerHand;
    private List<String> dealerHand;
    private List<String> dealerVisible; // Only dealer's visible cards
    private Integer playerValue;
    private Integer dealerValue;
    private String gameState; // PLAYING, BLACKJACK, WIN, LOSE, PUSH, BUST, DEALER_BLACKJACK
    private BigDecimal betAmount;
    private BigDecimal payout;
    private Boolean canHit;
    private Boolean canStand;
    private Boolean canDouble;
    private Boolean canSplit;

    // For continuing the game (not sent to client)
    private List<BlackjackEngine.Card> deck;
}
