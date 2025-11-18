package com.casino.game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrashGameResultData {
    private BigDecimal crashPoint; // Where the game crashed (e.g., 2.45x)
    private BigDecimal betAmount;
    private BigDecimal autoCashoutAt; // Auto cashout multiplier (optional)
    private BigDecimal cashedOutAt; // Actual cashout multiplier (null if didn't cashout)
    private Boolean isWin;
    private BigDecimal payout;
    private BigDecimal netProfit;
    private String gameState; // WAITING, RUNNING, CRASHED, CASHED_OUT
    private String serverSeed;
    private Long nonce;
    private String gameHash; // Hash of the crash point for verification
}
