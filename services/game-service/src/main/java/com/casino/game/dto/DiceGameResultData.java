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
public class DiceGameResultData {
    private Integer result; // 0-99
    private Integer targetNumber;
    private Boolean rollOver; // true = roll over, false = roll under
    private Boolean isWin;
    private BigDecimal betAmount;
    private BigDecimal multiplier;
    private BigDecimal payout;
    private BigDecimal netProfit;
    private BigDecimal winChance; // percentage
    private String serverSeed;
    private Long nonce;
}
