package com.casino.game.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_results", indexes = {
    @Index(name = "idx_session_id", columnList = "sessionId"),
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_game_code", columnList = "gameCode"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String gameCode;

    @Column(nullable = false)
    private Long roundNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoundOutcome outcome;

    // Financial details
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal betAmount;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal winAmount;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal netProfit;

    // Multiplier for this round
    @Column(precision = 10, scale = 2)
    private BigDecimal multiplier;

    // Balance before and after
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal balanceBefore;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal balanceAfter;

    // Game-specific result data (JSON stored as text)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String resultJson;

    // For verification and provably fair gaming
    @Column(nullable = false)
    private String serverSeed;

    private String clientSeed;

    @Column(nullable = false)
    private Long nonce;

    // Timestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (netProfit == null) {
            netProfit = winAmount.subtract(betAmount);
        }
    }

    public enum RoundOutcome {
        WIN,
        LOSS,
        DRAW,
        PUSH,           // Blackjack tie
        PLAYING,        // Round still in progress (Mines, Blackjack multi-action)
        JACKPOT,
        BONUS_TRIGGERED
    }

    // Helper method to check if this was a big win
    public boolean isBigWin() {
        return multiplier != null && multiplier.compareTo(new BigDecimal("10")) >= 0;
    }

    // Helper method to check if this was a mega win
    public boolean isMegaWin() {
        return multiplier != null && multiplier.compareTo(new BigDecimal("50")) >= 0;
    }
}
