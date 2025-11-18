package com.casino.game.repository;

import com.casino.game.entity.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, String> {

    Optional<GameSession> findByIdAndUserId(String id, String userId);

    Optional<GameSession> findFirstByUserIdAndStatusOrderByStartedAtDesc(
        String userId,
        GameSession.SessionStatus status
    );

    List<GameSession> findByUserIdOrderByStartedAtDesc(String userId);

    List<GameSession> findByUserIdAndGameCodeOrderByStartedAtDesc(String userId, String gameCode);

    List<GameSession> findByStatus(GameSession.SessionStatus status);

    @Query("SELECT gs FROM GameSession gs WHERE gs.status = :status " +
           "AND gs.lastActivityAt < :cutoffTime")
    List<GameSession> findInactiveSessions(
        @Param("status") GameSession.SessionStatus status,
        @Param("cutoffTime") LocalDateTime cutoffTime
    );

    @Modifying
    @Query("UPDATE GameSession gs SET gs.currentBalance = :balance, " +
           "gs.lastActivityAt = :now WHERE gs.id = :sessionId")
    int updateBalance(
        @Param("sessionId") String sessionId,
        @Param("balance") BigDecimal balance,
        @Param("now") LocalDateTime now
    );

    @Modifying
    @Query("UPDATE GameSession gs SET " +
           "gs.totalBet = gs.totalBet + :betAmount, " +
           "gs.totalWon = gs.totalWon + :winAmount, " +
           "gs.netProfit = gs.netProfit + :netProfit, " +
           "gs.roundsPlayed = gs.roundsPlayed + 1, " +
           "gs.roundsWon = CASE WHEN :isWin = true THEN gs.roundsWon + 1 ELSE gs.roundsWon END, " +
           "gs.roundsLost = CASE WHEN :isWin = false THEN gs.roundsLost + 1 ELSE gs.roundsLost END, " +
           "gs.biggestWin = CASE WHEN :winAmount > COALESCE(gs.biggestWin, 0) THEN :winAmount ELSE gs.biggestWin END, " +
           "gs.biggestLoss = CASE WHEN :betAmount > COALESCE(gs.biggestLoss, 0) AND :winAmount = 0 THEN :betAmount ELSE gs.biggestLoss END, " +
           "gs.lastActivityAt = :now " +
           "WHERE gs.id = :sessionId")
    int updateSessionStats(
        @Param("sessionId") String sessionId,
        @Param("betAmount") BigDecimal betAmount,
        @Param("winAmount") BigDecimal winAmount,
        @Param("netProfit") BigDecimal netProfit,
        @Param("isWin") boolean isWin,
        @Param("now") LocalDateTime now
    );

    @Modifying
    @Query("UPDATE GameSession gs SET gs.status = :status, gs.endedAt = :endedAt " +
           "WHERE gs.id = :sessionId")
    int endSession(
        @Param("sessionId") String sessionId,
        @Param("status") GameSession.SessionStatus status,
        @Param("endedAt") LocalDateTime endedAt
    );

    @Query("SELECT COUNT(gs) FROM GameSession gs WHERE gs.userId = :userId " +
           "AND gs.gameCode = :gameCode")
    long countUserSessionsForGame(@Param("userId") String userId, @Param("gameCode") String gameCode);

    @Query("SELECT SUM(gs.totalBet) FROM GameSession gs WHERE gs.userId = :userId")
    BigDecimal getTotalWageredByUser(@Param("userId") String userId);

    @Query("SELECT SUM(gs.totalWon) FROM GameSession gs WHERE gs.userId = :userId")
    BigDecimal getTotalWonByUser(@Param("userId") String userId);
}
