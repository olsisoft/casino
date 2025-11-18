package com.casino.game.repository;

import com.casino.game.entity.GameResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GameResultRepository extends JpaRepository<GameResult, String> {

    List<GameResult> findBySessionIdOrderByRoundNumberAsc(String sessionId);

    Page<GameResult> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    List<GameResult> findByUserIdAndGameCodeOrderByCreatedAtDesc(String userId, String gameCode);

    @Query("SELECT gr FROM GameResult gr WHERE gr.userId = :userId " +
           "AND gr.outcome = :outcome ORDER BY gr.createdAt DESC")
    List<GameResult> findByUserIdAndOutcome(
        @Param("userId") String userId,
        @Param("outcome") GameResult.RoundOutcome outcome
    );

    @Query("SELECT gr FROM GameResult gr WHERE gr.userId = :userId " +
           "AND gr.multiplier >= :minMultiplier ORDER BY gr.multiplier DESC")
    List<GameResult> findBigWins(
        @Param("userId") String userId,
        @Param("minMultiplier") BigDecimal minMultiplier,
        Pageable pageable
    );

    @Query("SELECT gr FROM GameResult gr WHERE gr.gameCode = :gameCode " +
           "AND gr.outcome = 'JACKPOT' ORDER BY gr.createdAt DESC")
    List<GameResult> findRecentJackpots(@Param("gameCode") String gameCode, Pageable pageable);

    @Query("SELECT COUNT(gr) FROM GameResult gr WHERE gr.userId = :userId " +
           "AND gr.outcome = :outcome")
    long countByUserIdAndOutcome(
        @Param("userId") String userId,
        @Param("outcome") GameResult.RoundOutcome outcome
    );

    @Query("SELECT SUM(gr.betAmount) FROM GameResult gr WHERE gr.userId = :userId " +
           "AND gr.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalBetByUserAndDateRange(
        @Param("userId") String userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT SUM(gr.winAmount) FROM GameResult gr WHERE gr.userId = :userId " +
           "AND gr.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalWinByUserAndDateRange(
        @Param("userId") String userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT AVG(gr.multiplier) FROM GameResult gr WHERE gr.gameCode = :gameCode " +
           "AND gr.outcome = 'WIN'")
    BigDecimal getAverageMultiplierForGame(@Param("gameCode") String gameCode);

    @Query("SELECT MAX(gr.roundNumber) FROM GameResult gr WHERE gr.sessionId = :sessionId")
    Long getLastRoundNumber(@Param("sessionId") String sessionId);
}
