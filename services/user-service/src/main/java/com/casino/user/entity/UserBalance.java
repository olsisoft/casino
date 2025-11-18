package com.casino.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_balances", indexes = {
    @Index(name = "idx_user_balance", columnList = "user_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBalance {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "virtual_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal virtualBalance;

    @Column(name = "real_balance", precision = 19, scale = 2)
    private BigDecimal realBalance;

    @Column(name = "bonus_balance", precision = 19, scale = 2)
    private BigDecimal bonusBalance;

    @Column(name = "locked_amount", precision = 19, scale = 2)
    private BigDecimal lockedAmount;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version; // For optimistic locking

    @PrePersist
    public void prePersist() {
        if (virtualBalance == null) virtualBalance = BigDecimal.valueOf(1000); // Start with 1000 virtual
        if (realBalance == null) realBalance = BigDecimal.ZERO;
        if (bonusBalance == null) bonusBalance = BigDecimal.ZERO;
        if (lockedAmount == null) lockedAmount = BigDecimal.ZERO;
        if (currency == null) currency = "USD";
    }

    public BigDecimal getTotalBalance() {
        return virtualBalance.add(realBalance).add(bonusBalance);
    }

    public BigDecimal getAvailableBalance() {
        return getTotalBalance().subtract(lockedAmount);
    }
}
