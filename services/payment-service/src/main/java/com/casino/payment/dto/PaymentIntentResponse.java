package com.casino.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Payment Intent Response DTO
 */
@Data
@Builder
public class PaymentIntentResponse {
    private Boolean success;
    private String paymentIntentId;
    private String clientSecret;      // For client-side confirmation
    private String status;             // requires_payment_method, requires_confirmation, succeeded, etc.
    private BigDecimal amount;
    private String currency;
    private String redirectUrl;        // For 3D Secure or bank redirects
    private String errorMessage;
}
