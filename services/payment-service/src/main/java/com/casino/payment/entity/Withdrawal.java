package com.casino.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "withdrawals", indexes = {
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Withdrawal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String transactionId;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WithdrawalStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WithdrawalMethod method;

    // Bank transfer details
    private String bankAccountNumber;
    private String bankRoutingNumber;
    private String bankAccountHolderName;

    // PayPal details
    private String paypalEmail;

    // Card details (for reference)
    private String cardLast4;

    private String reviewedBy; // Admin user ID
    private String reviewNotes;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    private LocalDateTime completedAt;

    private LocalDateTime reviewedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = WithdrawalStatus.PENDING_REVIEW;
        if (currency == null) currency = "USD";
    }

    @PreUpdate
    protected void onUpdate() {
        if (status == WithdrawalStatus.PROCESSING && processedAt == null) {
            processedAt = LocalDateTime.now();
        } else if (status == WithdrawalStatus.COMPLETED && completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }

    public enum WithdrawalStatus {
        PENDING_REVIEW,
        APPROVED,
        PROCESSING,
        COMPLETED,
        REJECTED,
        CANCELLED
    }

    public enum WithdrawalMethod {
        BANK_TRANSFER,
        PAYPAL,
        CARD
    }
}
