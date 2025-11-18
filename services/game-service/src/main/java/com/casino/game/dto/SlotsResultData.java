package com.casino.game.dto;

import com.casino.game.engine.SlotsGameEngine;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Slots game result data
 */
@Data
@Builder
public class SlotsResultData {
    private String[][] reels;                          // 5x3 grid of symbols
    private List<SlotsGameEngine.WinLine> winLines;    // Winning paylines
    private BigDecimal totalPayout;                    // Total win amount
    private BigDecimal profit;                         // Profit (payout - bet)
    private Boolean isWin;                             // Did player win?
    private Integer scatterCount;                      // Number of scatter symbols
    private Integer freeSpinsAwarded;                  // Free spins awarded (0 if none)

    // Provably fair data
    private String serverSeed;
    private String clientSeed;
    private Long nonce;
}
