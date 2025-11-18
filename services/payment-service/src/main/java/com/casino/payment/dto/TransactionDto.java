package com.casino.payment.dto;

import com.casino.payment.entity.PaymentMethod;
import com.casino.payment.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDto {
    private String id;
    private Transaction.TransactionType type;
    private Transaction.TransactionStatus status;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod.PaymentMethodType paymentMethodType;
    private BigDecimal platformFee;
    private BigDecimal paymentProcessorFee;
    private BigDecimal netAmount;
    private String description;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
