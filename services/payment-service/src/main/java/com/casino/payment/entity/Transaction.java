package com.casino.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_type", columnList = "type"),
    @Index(name = "idx_created_at", columnList = "createdAt"),
    @Index(name = "idx_stripe_payment_intent_id", columnList = "stripePaymentIntentId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentMethod.PaymentMethodType paymentMethodType;

    private String paymentMethodId;

    // Stripe-specific fields
    private String stripePaymentIntentId;
    private String stripeChargeId;

    // Fee tracking
    @Column(precision = 19, scale = 2)
    private BigDecimal platformFee;

    @Column(precision = 19, scale = 2)
    private BigDecimal paymentProcessorFee;

    @Column(precision = 19, scale = 2)
    private BigDecimal netAmount; // Amount after fees

    private String description;

    private String failureReason;

    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON for additional info

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    private LocalDateTime failedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (currency == null) currency = "USD";
        if (status == null) status = TransactionStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        if (status == TransactionStatus.COMPLETED && completedAt == null) {
            completedAt = LocalDateTime.now();
        } else if (status == TransactionStatus.FAILED && failedAt == null) {
            failedAt = LocalDateTime.now();
        }
    }

    public enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        REFUND,
        BONUS_CREDIT,
        PROMOTION
    }

    public enum TransactionStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED,
        REFUNDED
    }
}
