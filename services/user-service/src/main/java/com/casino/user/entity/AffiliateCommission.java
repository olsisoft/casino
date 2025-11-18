package com.casino.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "affiliate_commissions", indexes = {
    @Index(name = "idx_commission_affiliate", columnList = "affiliateId"),
    @Index(name = "idx_commission_period", columnList = "periodStart,periodEnd")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AffiliateCommission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String affiliateId;

    @Column(nullable = false)
    private String referralId; // The referral that generated this commission

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommissionType type;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(precision = 19, scale = 2)
    private BigDecimal referralRevenue; // Revenue that generated this commission

    @Column(precision = 5, scale = 2)
    private BigDecimal commissionRate; // Rate applied

    // Period tracking
    private LocalDate periodStart;
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommissionStatus status;

    private String payoutId; // Reference to payout transaction

    private LocalDateTime paidAt;

    @Column(length = 1000)
    private String notes;

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

    public enum CommissionType {
        REVENUE_SHARE,  // Percentage of revenue
        CPA,            // Cost per acquisition
        HYBRID          // Combination
    }

    public enum CommissionStatus {
        PENDING,        // Not yet approved
        APPROVED,       // Approved, ready for payout
        PAID,           // Paid out
        CANCELLED,      // Cancelled (fraud, chargeback, etc.)
        HELD            // On hold for review
    }
}
