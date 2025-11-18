package com.casino.user.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalanceDto {
    private BigDecimal virtualBalance;
    private BigDecimal realBalance;
    private BigDecimal bonusBalance;
    private BigDecimal totalBalance;
    private BigDecimal availableBalance;
    private BigDecimal lockedAmount;
    private String currency;
}
