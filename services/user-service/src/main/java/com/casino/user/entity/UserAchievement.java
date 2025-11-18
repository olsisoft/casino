package com.casino.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_achievements", indexes = {
    @Index(name = "idx_user_achievement", columnList = "userId,achievementId"),
    @Index(name = "idx_user_unlocked", columnList = "userId,unlockedAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String achievementId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "achievementId", insertable = false, updatable = false)
    private Achievement achievement;

    // Progress tracking
    @Column(precision = 19, scale = 2)
    private BigDecimal currentAmount; // For wagering, winning progress

    private Long currentCount; // For games played, wins count, etc.

    private Integer currentDays; // For streak tracking

    @Column(precision = 5, scale = 2)
    private BigDecimal progressPercentage; // 0-100%

    @Column(nullable = false)
    private Boolean isUnlocked = false;

    private LocalDateTime unlockedAt;

    @Column(nullable = false)
    private Boolean isRewardClaimed = false;

    private LocalDateTime rewardClaimedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (currentAmount == null) currentAmount = BigDecimal.ZERO;
        if (currentCount == null) currentCount = 0L;
        if (currentDays == null) currentDays = 0;
        if (progressPercentage == null) progressPercentage = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Calculate progress percentage based on achievement type
     */
    public void calculateProgress() {
        if (achievement == null) {
            progressPercentage = BigDecimal.ZERO;
            return;
        }

        BigDecimal progress = BigDecimal.ZERO;

        switch (achievement.getType()) {
            case MILESTONE, ACCUMULATIVE -> {
                if (achievement.getTargetAmount() != null &&
                    achievement.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
                    progress = currentAmount
                        .divide(achievement.getTargetAmount(), 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                } else if (achievement.getTargetCount() != null && achievement.getTargetCount() > 0) {
                    progress = BigDecimal.valueOf(currentCount)
                        .divide(BigDecimal.valueOf(achievement.getTargetCount()), 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                }
            }
            case STREAK -> {
                if (achievement.getTargetDays() != null && achievement.getTargetDays() > 0) {
                    progress = BigDecimal.valueOf(currentDays)
                        .divide(BigDecimal.valueOf(achievement.getTargetDays()), 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                }
            }
            case CHALLENGE -> {
                if (achievement.getTargetCount() != null && achievement.getTargetCount() > 0) {
                    progress = BigDecimal.valueOf(currentCount)
                        .divide(BigDecimal.valueOf(achievement.getTargetCount()), 4, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                }
            }
        }

        // Cap at 100%
        if (progress.compareTo(BigDecimal.valueOf(100)) > 0) {
            progress = BigDecimal.valueOf(100);
        }

        this.progressPercentage = progress.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Check if achievement criteria is met
     */
    public boolean isCriteriaMet() {
        if (achievement == null) return false;

        return switch (achievement.getType()) {
            case MILESTONE, ACCUMULATIVE -> {
                if (achievement.getTargetAmount() != null) {
                    yield currentAmount.compareTo(achievement.getTargetAmount()) >= 0;
                } else if (achievement.getTargetCount() != null) {
                    yield currentCount >= achievement.getTargetCount();
                }
                yield false;
            }
            case STREAK -> achievement.getTargetDays() != null && currentDays >= achievement.getTargetDays();
            case CHALLENGE -> achievement.getTargetCount() != null && currentCount >= achievement.getTargetCount();
        };
    }

    /**
     * Unlock the achievement
     */
    public void unlock() {
        this.isUnlocked = true;
        this.unlockedAt = LocalDateTime.now();
        this.progressPercentage = BigDecimal.valueOf(100);
    }

    /**
     * Claim the reward
     */
    public void claimReward() {
        this.isRewardClaimed = true;
        this.rewardClaimedAt = LocalDateTime.now();
    }
}
