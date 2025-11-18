package com.casino.game.dto;

import com.casino.game.engine.PokerGameEngine;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Texas Hold'em Poker game result data
 */
@Data
@Builder
public class PokerResultData {
    private List<String> playerHole;                  // Player's 2 hole cards
    private List<String> dealerHole;                  // Dealer's 2 hole cards
    private List<String> communityCards;              // 5 community cards (flop, turn, river)
    private PokerGameEngine.HandRank playerHandRank;  // Player's best hand rank
    private PokerGameEngine.HandRank dealerHandRank;  // Dealer's best hand rank
    private List<String> playerBestCards;             // Player's best 5 cards
    private List<String> dealerBestCards;             // Dealer's best 5 cards
    private String result;                            // PLAYER_WIN, DEALER_WIN, TIE
    private BigDecimal payout;                        // Total payout
    private BigDecimal profit;                        // Profit (payout - bet)
    private Boolean isWin;                            // Did player win?

    // Provably fair data
    private String serverSeed;
    private String clientSeed;
    private Long nonce;
}
