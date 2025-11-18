package com.casino.game.dto;

import com.casino.game.engine.BaccaratGameEngine;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Baccarat game result data
 */
@Data
@Builder
public class BaccaratResultData {
    private List<String> playerCards;                 // Player's cards (e.g., ["A♠", "5♥"])
    private List<String> bankerCards;                 // Banker's cards
    private Integer playerScore;                      // Player's score (0-9)
    private Integer bankerScore;                      // Banker's score (0-9)
    private BaccaratGameEngine.Winner winner;         // Who won (PLAYER, BANKER, TIE)
    private BaccaratGameEngine.BetType betType;       // What player bet on
    private BigDecimal payout;                        // Total payout
    private BigDecimal profit;                        // Profit (payout - bet)
    private Boolean isWin;                            // Did player win?

    // Provably fair data
    private String serverSeed;
    private String clientSeed;
    private Long nonce;
}
