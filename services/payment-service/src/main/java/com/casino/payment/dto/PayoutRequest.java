package com.casino.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Payout Request DTO
 */
@Data
@Builder
public class PayoutRequest {
    private String userId;
    private String transactionId;
    private BigDecimal amount;
    private String currency;
    private String payoutMethod;    // bank_transfer, crypto, paypal, etc.
    private String destination;     // Bank account, crypto address, PayPal email
    private String bankName;
    private String accountNumber;
    private String routingNumber;   // For US banks
    private String iban;            // For international banks
    private String swiftCode;
}
