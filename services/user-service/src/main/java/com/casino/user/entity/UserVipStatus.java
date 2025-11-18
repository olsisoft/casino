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
@Table(name = "user_vip_status", indexes = {
    @Index(name = "idx_user_vip", columnList = "userId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVipStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String currentTierId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "currentTierId", insertable = false, updatable = false)
    private VipTier currentTier;

    // Progress tracking
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal totalWagering = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal totalDeposit = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer daysActive = 0;

    @Column(precision = 5, scale = 2)
    private BigDecimal progressToNextTier; // Percentage 0-100

    // Points system
    @Column(nullable = false)
    private Long vipPoints = 0L; // Accumulative points

    @Column(nullable = false)
    private Long monthlyPoints = 0L; // Points this month (resets monthly)

    private LocalDate monthlyPointsResetDate;

    // Tier history
    private LocalDateTime tierAchievedAt;
    private LocalDateTime lastTierUpgradeAt;
    private LocalDateTime lastTierDowngradeAt;

    // Benefits tracking
    private LocalDateTime lastMonthlyBonusClaimedAt;
    private LocalDateTime lastWeeklyBonusClaimedAt;
    private LocalDateTime lastBirthdayBonusClaimedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (monthlyPointsResetDate == null) {
            monthlyPointsResetDate = LocalDate.now().plusMonths(1).withDayOfMonth(1);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Add VIP points (1 point per $1 wagered typically)
     */
    public void addPoints(Long points) {
        this.vipPoints += points;
        this.monthlyPoints += points;
    }

    /**
     * Reset monthly points
     */
    public void resetMonthlyPoints() {
        this.monthlyPoints = 0L;
        this.monthlyPointsResetDate = LocalDate.now().plusMonths(1).withDayOfMonth(1);
    }

    /**
     * Check if monthly bonus can be claimed
     */
    public boolean canClaimMonthlyBonus() {
        if (lastMonthlyBonusClaimedAt == null) return true;
        return lastMonthlyBonusClaimedAt.isBefore(
            LocalDateTime.now().minusMonths(1)
        );
    }

    /**
     * Check if weekly bonus can be claimed
     */
    public boolean canClaimWeeklyBonus() {
        if (lastWeeklyBonusClaimedAt == null) return true;
        return lastWeeklyBonusClaimedAt.isBefore(
            LocalDateTime.now().minusWeeks(1)
        );
    }

    /**
     * Check if birthday bonus can be claimed this year
     */
    public boolean canClaimBirthdayBonus() {
        if (lastBirthdayBonusClaimedAt == null) return true;
        return lastBirthdayBonusClaimedAt.getYear() < LocalDateTime.now().getYear();
    }
}
