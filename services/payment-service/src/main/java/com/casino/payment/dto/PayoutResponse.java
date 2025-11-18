package com.casino.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Payout Response DTO
 */
@Data
@Builder
public class PayoutResponse {
    private Boolean success;
    private String payoutId;
    private String status;              // pending, paid, failed, cancelled
    private BigDecimal amount;
    private String currency;
    private Long estimatedArrival;      // Unix timestamp
    private String trackingNumber;
    private String errorMessage;
}
