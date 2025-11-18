package com.casino.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "aml_transactions", indexes = {
    @Index(name = "idx_aml_user", columnList = "userId"),
    @Index(name = "idx_aml_status", columnList = "status"),
    @Index(name = "idx_aml_risk", columnList = "riskLevel"),
    @Index(name = "idx_aml_created", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmlTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    private String transactionId; // Reference to actual transaction

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AmlStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;

    @Column(nullable = false)
    private Integer riskScore; // 0-100

    // Risk indicators
    private Boolean isStructured; // Structuring (breaking up large transactions)
    private Boolean isUnusualPattern; // Unusual transaction pattern
    private Boolean isHighVelocity; // Too many transactions in short time
    private Boolean isRoundAmount; // Suspiciously round amounts
    private Boolean isHighRisk; // High-risk jurisdiction or method
    private Boolean isPep; // Transaction by PEP
    private Boolean isSanctioned; // Sanctioned country/entity

    // Pattern detection
    private Integer transactionsLast24h;
    private Integer transactionsLast7d;

    @Column(precision = 19, scale = 2)
    private BigDecimal volumeLast24h;

    @Column(precision = 19, scale = 2)
    private BigDecimal volumeLast7d;

    // Location data
    private String ipAddress;
    private String country;
    private String city;

    // Review
    private String reviewedBy; // Admin user ID
    private LocalDateTime reviewedAt;

    @Column(length = 2000)
    private String reviewNotes;

    @Enumerated(EnumType.STRING)
    private ReviewDecision reviewDecision;

    // SAR (Suspicious Activity Report)
    private Boolean sarFiled;
    private String sarId;
    private LocalDateTime sarFiledAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        TRANSFER,
        BONUS,
        REFUND
    }

    public enum AmlStatus {
        PENDING_REVIEW,
        IN_REVIEW,
        CLEARED,
        FLAGGED,
        BLOCKED,
        REPORTED
    }

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum ReviewDecision {
        APPROVED,
        REJECTED,
        REQUIRES_INVESTIGATION,
        ESCALATED
    }
}
