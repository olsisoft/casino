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
@Table(name = "leaderboard_entries", indexes = {
    @Index(name = "idx_user_period", columnList = "userId,periodType,periodDate"),
    @Index(name = "idx_period_score", columnList = "periodType,periodDate,score")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodType periodType;

    @Column(nullable = false)
    private LocalDate periodDate; // Date identifier for the period

    // Metrics
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal totalWagered;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal totalWon;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal netProfit;

    @Column(nullable = false)
    private Long gamesPlayed;

    @Column(nullable = false)
    private Long gamesWon;

    // Score used for ranking (can be calculated from metrics)
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal score;

    private Integer rank;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onCreateOrUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    public enum PeriodType {
        DAILY,
        WEEKLY,
        MONTHLY,
        ALL_TIME
    }

    public BigDecimal getWinRate() {
        if (gamesPlayed == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(gamesWon)
            .divide(BigDecimal.valueOf(gamesPlayed), 4, java.math.RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
}
