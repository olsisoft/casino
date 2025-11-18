package com.casino.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promo_codes", indexes = {
    @Index(name = "idx_code", columnList = "code", unique = true),
    @Index(name = "idx_active", columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromoType promoType;

    // Bonus configuration
    @Column(precision = 19, scale = 2)
    private BigDecimal bonusAmount; // Fixed amount bonus

    @Column(precision = 5, scale = 2)
    private BigDecimal bonusPercentage; // Percentage bonus (e.g., 100% = double)

    @Column(precision = 19, scale = 2)
    private BigDecimal maxBonusAmount; // Max bonus if using percentage

    @Column(precision = 19, scale = 2)
    private BigDecimal minDepositAmount; // Minimum deposit to activate

    // Wagering requirements
    @Column(nullable = false)
    private Integer wagerMultiplier; // e.g., 30x

    // Usage limits
    @Column(nullable = false)
    private Integer maxUses; // Total times code can be used (0 = unlimited)

    @Column(nullable = false)
    private Integer maxUsesPerUser; // Max times per user (usually 1)

    @Column(nullable = false)
    private Integer currentUses; // Current usage count

    // Validity
    @Column(nullable = false)
    private Boolean active;

    private LocalDateTime validFrom;

    private LocalDateTime validUntil;

    // Restrictions
    private String allowedGameTypes; // Comma-separated game types

    private Boolean newUsersOnly; // Only for new users

    private Integer minAccountAgeDays; // Minimum account age in days

    // Metadata
    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy; // Admin user who created this

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (active == null) active = true;
        if (currentUses == null) currentUses = 0;
        if (maxUses == null) maxUses = 0; // 0 = unlimited
        if (maxUsesPerUser == null) maxUsesPerUser = 1;
        if (wagerMultiplier == null) wagerMultiplier = 30;
        if (newUsersOnly == null) newUsersOnly = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum PromoType {
        DEPOSIT_BONUS,      // Bonus on deposit
        NO_DEPOSIT_BONUS,   // Free bonus without deposit
        FREE_SPINS,         // Free spins on slots
        CASHBACK,           // Cashback percentage
        CUSTOM              // Custom promo
    }

    // Helper methods
    public boolean isValid() {
        if (!active) return false;

        LocalDateTime now = LocalDateTime.now();
        if (validFrom != null && now.isBefore(validFrom)) return false;
        if (validUntil != null && now.isAfter(validUntil)) return false;

        if (maxUses > 0 && currentUses >= maxUses) return false;

        return true;
    }

    public boolean canBeUsedBy(User user) {
        if (!isValid()) return false;

        if (newUsersOnly != null && newUsersOnly) {
            // Check if user has made any deposits
            // This would need to be checked in the service layer
        }

        if (minAccountAgeDays != null && minAccountAgeDays > 0) {
            long accountAgeDays = java.time.temporal.ChronoUnit.DAYS.between(
                user.getCreatedAt(), LocalDateTime.now()
            );
            if (accountAgeDays < minAccountAgeDays) {
                return false;
            }
        }

        return true;
    }

    public BigDecimal calculateBonusAmount(BigDecimal depositAmount) {
        if (bonusAmount != null) {
            return bonusAmount; // Fixed amount
        }

        if (bonusPercentage != null) {
            BigDecimal calculatedBonus = depositAmount
                .multiply(bonusPercentage)
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

            if (maxBonusAmount != null) {
                return calculatedBonus.min(maxBonusAmount);
            }

            return calculatedBonus;
        }

        return BigDecimal.ZERO;
    }

    public void incrementUsage() {
        currentUses++;
    }
}
