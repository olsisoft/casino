package com.casino.game.dto;

import com.casino.game.service.RouletteEngine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouletteResultData {
    private Integer winningNumber;
    private String color;
    private Boolean isEven;
    private Boolean isRed;
    private Boolean isBlack;
    private Integer dozen;
    private Integer column;
    private String half; // LOW or HIGH

    private BigDecimal totalBet;
    private BigDecimal totalPayout;
    private BigDecimal netProfit;

    private Map<String, BigDecimal> bets; // bet type -> bet amount
    private Map<String, BigDecimal> winningBets; // winning bet type -> payout

    private RouletteEngine.RouletteType rouletteType;
}
