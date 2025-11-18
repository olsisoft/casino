package com.casino.game.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "game_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String gameCode;

    @Column(nullable = false)
    private String gameName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameType gameType;

    @Column(nullable = false)
    private String description;

    private String imageUrl;

    @Column(nullable = false)
    private Boolean active;

    // RTP (Return to Player) percentage
    @Column(precision = 5, scale = 2, nullable = false)
    private BigDecimal rtpPercentage;

    // Min and max bet amounts
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal minBet;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal maxBet;

    // Game-specific configuration (JSON stored as text)
    @Column(columnDefinition = "TEXT")
    private String configJson;

    // For slots: number of reels, paylines, symbols
    private Integer reels;
    private Integer rows;
    private Integer paylines;

    // Popularity metrics
    @Column(nullable = false)
    private Long totalPlays;

    @Column(nullable = false)
    private Long activePlayers;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal totalWagered;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal totalPaidOut;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (active == null) active = true;
        if (totalPlays == null) totalPlays = 0L;
        if (activePlayers == null) activePlayers = 0L;
        if (totalWagered == null) totalWagered = BigDecimal.ZERO;
        if (totalPaidOut == null) totalPaidOut = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum GameType {
        SLOTS,
        BLACKJACK,
        ROULETTE,
        POKER,
        VIDEO_POKER,
        CRAPS,
        SIC_BO,
        BACCARAT,
        DICE,
        MINES,
        CRASH,
        COIN_FLIP
    }
}
