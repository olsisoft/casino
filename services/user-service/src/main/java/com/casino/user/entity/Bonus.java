package com.casino.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bonuses", indexes = {
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_expires_at", columnList = "expiresAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bonus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BonusType bonusType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BonusStatus status;

    // Bonus amount
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    // Amount already wagered
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal wageredAmount;

    // Required wager amount to unlock bonus (wagering requirement)
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal requiredWagerAmount;

    // Wagering multiplier (e.g., 30x means must wager 30 times the bonus amount)
    @Column(nullable = false)
    private Integer wagerMultiplier;

    // Bonus details
    private String title;
    private String description;

    // Promo code (if applicable)
    private String promoCode;

    // Restrictions
    @Column(precision = 10, scale = 2)
    private BigDecimal minBetAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal maxBetAmount;

    // Allowed game types (comma-separated) - null means all games allowed
    private String allowedGameTypes;

    // Dates
    @Column(nullable = false)
    private LocalDateTime issuedAt;

    private LocalDateTime activatedAt;

    private LocalDateTime expiresAt;

    private LocalDateTime completedAt;

    private LocalDateTime cancelledAt;

    @PrePersist
    protected void onCreate() {
        if (issuedAt == null) {
            issuedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = BonusStatus.PENDING;
        }
        if (wageredAmount == null) {
            wageredAmount = BigDecimal.ZERO;
        }
    }

    public enum BonusType {
        WELCOME_BONUS,      // First deposit bonus
        DEPOSIT_BONUS,      // Regular deposit bonus
        NO_DEPOSIT_BONUS,   // Free bonus without deposit
        DAILY_REWARD,       // Daily login reward
        WEEKLY_REWARD,      // Weekly reward
        CASHBACK,           // Percentage of losses back
        RELOAD_BONUS,       // Bonus on subsequent deposits
        PROMO_CODE,         // Promo code bonus
        REFERRAL_BONUS,     // Referral reward
        VIP_BONUS,          // VIP tier bonus
        ACHIEVEMENT_BONUS,  // Achievement reward
        TOURNAMENT_PRIZE    // Tournament winnings
    }

    public enum BonusStatus {
        PENDING,        // Bonus issued but not activated
        ACTIVE,         // Bonus active, wagering in progress
        COMPLETED,      // Wagering requirement met, bonus unlocked
        EXPIRED,        // Bonus expired before completion
        CANCELLED,      // Bonus cancelled by user or admin
        FORFEITED       // Bonus forfeited (e.g., withdrawal before meeting requirements)
    }

    // Helper methods
    public BigDecimal getRemainingWagerAmount() {
        return requiredWagerAmount.subtract(wageredAmount).max(BigDecimal.ZERO);
    }

    public BigDecimal getWagerProgress() {
        if (requiredWagerAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }
        return wageredAmount
            .divide(requiredWagerAmount, 4, java.math.RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean canBeActivated() {
        return status == BonusStatus.PENDING && !isExpired();
    }

    public boolean isActive() {
        return status == BonusStatus.ACTIVE && !isExpired();
    }
}
