package com.casino.game.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_sessions", indexes = {
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_game_code", columnList = "gameCode"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String gameCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    // Session financial tracking
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal startingBalance;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal currentBalance;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal totalBet;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal totalWon;

    @Column(precision = 19, scale = 2)
    private BigDecimal netProfit;

    // Session statistics
    @Column(nullable = false)
    private Integer roundsPlayed;

    @Column(nullable = false)
    private Integer roundsWon;

    @Column(nullable = false)
    private Integer roundsLost;

    @Column(precision = 19, scale = 2)
    private BigDecimal biggestWin;

    @Column(precision = 19, scale = 2)
    private BigDecimal biggestLoss;

    // Session timing
    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private Long durationSeconds;

    @Column(nullable = false)
    private LocalDateTime lastActivityAt;

    // Balance type used
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BalanceType balanceType;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        startedAt = now;
        lastActivityAt = now;
        if (status == null) status = SessionStatus.ACTIVE;
        if (totalBet == null) totalBet = BigDecimal.ZERO;
        if (totalWon == null) totalWon = BigDecimal.ZERO;
        if (netProfit == null) netProfit = BigDecimal.ZERO;
        if (roundsPlayed == null) roundsPlayed = 0;
        if (roundsWon == null) roundsWon = 0;
        if (roundsLost == null) roundsLost = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        lastActivityAt = LocalDateTime.now();
        if (status == SessionStatus.COMPLETED && endedAt != null && startedAt != null) {
            durationSeconds = java.time.Duration.between(startedAt, endedAt).getSeconds();
        }
    }

    public enum SessionStatus {
        ACTIVE,
        COMPLETED,
        ABANDONED,
        EXPIRED
    }

    public enum BalanceType {
        VIRTUAL,
        REAL,
        BONUS
    }
}
