package com.casino.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "affiliates", indexes = {
    @Index(name = "idx_affiliate_user", columnList = "userId"),
    @Index(name = "idx_affiliate_code", columnList = "affiliateCode")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Affiliate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false, unique = true, length = 20)
    private String affiliateCode; // Unique referral code

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AffiliateStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AffiliateTier tier; // Standard, Bronze, Silver, Gold, Platinum

    // Commission structure
    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal commissionPercentage; // Revenue share %

    @Column(precision = 5, scale = 2)
    private BigDecimal cpaAmount; // Cost Per Acquisition (fixed amount per new player)

    // Statistics
    @Column(nullable = false)
    private Long totalReferrals = 0L;

    @Column(nullable = false)
    private Long activeReferrals = 0L; // Players who made deposit

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal pendingEarnings = BigDecimal.ZERO; // Not yet paid out

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal paidEarnings = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal lifetimeReferralRevenue = BigDecimal.ZERO; // Total revenue from referrals

    // Payment info
    @Column(precision = 19, scale = 2)
    private BigDecimal minimumPayout; // Minimum amount for withdrawal

    private String paymentMethod; // Bank, Crypto, etc.
    private String paymentDetails; // Encrypted payment info

    private LocalDateTime lastPayoutAt;

    // Marketing
    private String websiteUrl;
    private String trafficSource;

    @Column(length = 2000)
    private String notes;

    @Column(nullable = false)
    private Boolean isActive = true;

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

    public enum AffiliateStatus {
        PENDING,    // Application pending
        ACTIVE,     // Active affiliate
        SUSPENDED,  // Temporarily suspended
        BANNED      // Permanently banned
    }

    public enum AffiliateTier {
        STANDARD,   // 0-10 active referrals
        BRONZE,     // 11-25 active referrals
        SILVER,     // 26-50 active referrals
        GOLD,       // 51-100 active referrals
        PLATINUM    // 100+ active referrals
    }

    /**
     * Calculate conversion rate
     */
    public BigDecimal getConversionRate() {
        if (totalReferrals == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(activeReferrals)
            .divide(BigDecimal.valueOf(totalReferrals), 4, java.math.RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
}
