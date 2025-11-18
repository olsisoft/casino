package com.casino.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Payment Intent Request DTO
 */
@Data
@Builder
public class PaymentIntentRequest {
    private String userId;
    private String transactionId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod; // card, bank_transfer, crypto, etc.
    private String returnUrl;     // For 3D Secure redirects
}
