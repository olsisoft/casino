package com.casino.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "achievements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String code; // Unique identifier (e.g., "FIRST_WIN", "HIGH_ROLLER")

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AchievementCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AchievementTier tier; // BRONZE, SILVER, GOLD, PLATINUM, DIAMOND

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AchievementType type; // MILESTONE, STREAK, ACCUMULATIVE, CHALLENGE

    private String iconUrl;

    // Criteria
    @Column(precision = 19, scale = 2)
    private BigDecimal targetAmount; // For wagering, winning targets

    private Long targetCount; // For games played, wins count, etc.

    private Integer targetDays; // For streaks

    // Rewards
    @Column(precision = 19, scale = 2)
    private BigDecimal rewardAmount;

    private Integer rewardPoints; // XP or achievement points

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean isHidden = false; // Hidden until unlocked

    private Integer displayOrder; // For sorting

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

    public enum AchievementCategory {
        GAMES,          // Game-related achievements
        WAGERING,       // Wagering milestones
        WINNING,        // Winning achievements
        SOCIAL,         // Friend, chat related
        SPECIAL,        // Special events, seasonal
        PROGRESSION,    // Level-up, XP related
        COLLECTION,     // Collect all game types, etc.
        STREAK          // Login streaks, win streaks
    }

    public enum AchievementTier {
        BRONZE,
        SILVER,
        GOLD,
        PLATINUM,
        DIAMOND
    }

    public enum AchievementType {
        MILESTONE,      // One-time milestone (play 100 games)
        STREAK,         // Consecutive actions (7-day login streak)
        ACCUMULATIVE,   // Cumulative progress (wager $10,000 total)
        CHALLENGE       // Special challenge (win 5 in a row)
    }
}
