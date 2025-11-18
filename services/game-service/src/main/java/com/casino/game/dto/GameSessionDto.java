package com.casino.game.dto;

import com.casino.game.entity.GameSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSessionDto {
    private String id;
    private String userId;
    private String gameCode;
    private GameSession.SessionStatus status;
    private BigDecimal startingBalance;
    private BigDecimal currentBalance;
    private BigDecimal totalBet;
    private BigDecimal totalWon;
    private BigDecimal netProfit;
    private Integer roundsPlayed;
    private Integer roundsWon;
    private Integer roundsLost;
    private BigDecimal biggestWin;
    private BigDecimal biggestLoss;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Long durationSeconds;
    private GameSession.BalanceType balanceType;
}
