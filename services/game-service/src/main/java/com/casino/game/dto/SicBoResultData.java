package com.casino.game.dto;

import com.casino.game.engine.SicBoGameEngine;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Sic Bo game result data
 */
@Data
@Builder
public class SicBoResultData {
    private List<Integer> dice;                                          // Three dice values (1-6)
    private Integer total;                                               // Sum of all three dice
    private Map<SicBoGameEngine.BetType, BigDecimal> bets;              // All bets placed
    private Map<SicBoGameEngine.BetType, SicBoGameEngine.BetResult> results;  // Result for each bet
    private BigDecimal totalBet;                                        // Total amount bet
    private BigDecimal totalPayout;                                     // Total payout
    private BigDecimal profit;                                          // Profit (payout - bet)
    private Boolean isWin;                                              // Did player win overall?

    // Provably fair data
    private String serverSeed;
    private String clientSeed;
    private Long nonce;
}
