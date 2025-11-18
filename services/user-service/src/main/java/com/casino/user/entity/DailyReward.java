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
@Table(name = "daily_rewards", indexes = {
    @Index(name = "idx_user_date", columnList = "userId,rewardDate", unique = true),
    @Index(name = "idx_user_streak", columnList = "userId,currentStreak")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyReward {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private LocalDate rewardDate;

    @Column(nullable = false)
    private Integer currentStreak;

    @Column(nullable = false)
    private Integer maxStreak;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal rewardAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RewardType rewardType;

    private String rewardDetails; // JSON for additional reward info

    @Column(nullable = false)
    private Boolean claimed;

    private LocalDateTime claimedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (claimed == null) {
            claimed = false;
        }
    }

    public enum RewardType {
        COINS,          // Currency reward
        FREE_SPINS,     // Free spins
        BONUS,          // Bonus with wagering requirement
        MULTIPLIER,     // Temporary multiplier boost
        CASHBACK_BOOST  // Increased cashback percentage
    }
}
