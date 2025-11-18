package com.casino.payment.dto;

import com.casino.payment.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositResponse {
    private String transactionId;
    private Transaction.TransactionStatus status;
    private BigDecimal amount;
    private String currency;
    private String stripePaymentIntentId;
    private String clientSecret; // For Stripe Elements
    private Boolean requiresAction;
    private String nextActionUrl;
}
