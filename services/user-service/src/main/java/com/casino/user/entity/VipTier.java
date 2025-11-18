package com.casino.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vip_tiers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VipTier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String name; // Bronze, Silver, Gold, Platinum, Diamond, VIP, SVIP

    @Column(nullable = false, unique = true)
    private Integer level; // 1-7

    @Column(length = 1000)
    private String description;

    private String badgeIconUrl;
    private String badgeColor;

    // Requirements to reach this tier
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal minWagering; // Total wagering required

    @Column(precision = 19, scale = 2)
    private BigDecimal minDeposit; // Total deposit required

    private Integer minDaysActive; // Days active required

    // Benefits
    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal cashbackPercentage; // Additional cashback %

    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal bonusMultiplier; // Bonus multiplier (1.0 = no boost, 1.5 = 50% more)

    @Column(precision = 19, scale = 2)
    private BigDecimal monthlyBonus; // Fixed monthly bonus for this tier

    @Column(precision = 19, scale = 2)
    private BigDecimal weeklyBonus; // Fixed weekly bonus

    @Column(precision = 19, scale = 2)
    private BigDecimal birthdayBonus; // Birthday bonus amount

    private Integer prioritySupport; // Support priority level (higher = better)

    @Column(precision = 19, scale = 2)
    private BigDecimal withdrawalLimitDaily; // Higher withdrawal limits

    @Column(precision = 19, scale = 2)
    private BigDecimal withdrawalLimitMonthly;

    private Integer withdrawalPriorityHours; // Faster withdrawal processing

    private Boolean accessToVipGames; // Access to exclusive games
    private Boolean accessToVipTournaments; // Access to VIP tournaments
    private Boolean personalAccountManager; // Dedicated account manager
    private Boolean exclusivePromotions; // Access to exclusive promos

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
}
