package com.casino.user.repository;

import com.casino.user.entity.LeaderboardEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderboardRepository extends JpaRepository<LeaderboardEntry, String> {

    Optional<LeaderboardEntry> findByUserIdAndPeriodTypeAndPeriodDate(
        String userId,
        LeaderboardEntry.PeriodType periodType,
        LocalDate periodDate
    );

    List<LeaderboardEntry> findByPeriodTypeAndPeriodDateOrderByScoreDesc(
        LeaderboardEntry.PeriodType periodType,
        LocalDate periodDate,
        Pageable pageable
    );

    @Query("SELECT e FROM LeaderboardEntry e WHERE e.periodType = :periodType AND e.periodDate = :periodDate ORDER BY e.score DESC")
    List<LeaderboardEntry> findTopByPeriod(
        LeaderboardEntry.PeriodType periodType,
        LocalDate periodDate,
        Pageable pageable
    );

    @Query("SELECT COUNT(e) + 1 FROM LeaderboardEntry e WHERE e.periodType = :periodType AND e.periodDate = :periodDate AND e.score > :score")
    Long findRankByScore(
        LeaderboardEntry.PeriodType periodType,
        LocalDate periodDate,
        java.math.BigDecimal score
    );

    void deleteByPeriodTypeAndPeriodDate(
        LeaderboardEntry.PeriodType periodType,
        LocalDate periodDate
    );
}
