package com.casino.game.dto;

import com.casino.game.entity.GameResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayRoundResponse {
    private String resultId;
    private Long roundNumber;
    private GameResult.RoundOutcome outcome;
    private BigDecimal betAmount;
    private BigDecimal winAmount;
    private BigDecimal netProfit;
    private BigDecimal multiplier;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String resultData; // JSON string with game-specific data
    private String serverSeed;
    private Long nonce;
    private boolean isBigWin;
    private boolean isMegaWin;
}
