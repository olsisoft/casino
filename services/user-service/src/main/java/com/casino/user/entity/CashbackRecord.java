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
@Table(name = "cashback_records", indexes = {
    @Index(name = "idx_user_period", columnList = "userId,periodStart,periodEnd"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashbackRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CashbackPeriod cashbackPeriod;

    @Column(nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false)
    private LocalDate periodEnd;

    // Total wagered in period
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal totalWagered;

    // Total won in period
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal totalWon;

    // Net loss (wagered - won, only if negative)
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal netLoss;

    // Cashback percentage
    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal cashbackPercentage;

    // Calculated cashback amount
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal cashbackAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CashbackStatus status;

    @Column(nullable = false)
    private LocalDateTime calculatedAt;

    private LocalDateTime claimedAt;

    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        if (calculatedAt == null) {
            calculatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = CashbackStatus.PENDING;
        }
    }

    public enum CashbackPeriod {
        DAILY,
        WEEKLY,
        MONTHLY
    }

    public enum CashbackStatus {
        PENDING,        // Calculated but not yet claimable
        CLAIMABLE,      // Ready to claim
        CLAIMED,        // Claimed by user
        EXPIRED         // Expired before claiming
    }

    public boolean isClaimable() {
        return status == CashbackStatus.CLAIMABLE &&
               (expiresAt == null || LocalDateTime.now().isBefore(expiresAt));
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
