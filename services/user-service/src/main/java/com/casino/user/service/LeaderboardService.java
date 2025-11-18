package com.casino.user.service;

import com.casino.user.entity.LeaderboardEntry;
import com.casino.user.repository.LeaderboardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;

    private static final LocalDate ALL_TIME_DATE = LocalDate.of(2000, 1, 1);

    /**
     * Update user stats for leaderboards
     */
    @Transactional
    public void updateUserStats(
        String userId,
        String username,
        BigDecimal wagered,
        BigDecimal won,
        boolean isWin
    ) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate monthStart = today.with(TemporalAdjusters.firstDayOfMonth());

        // Update all periods
        updatePeriodEntry(userId, username, LeaderboardEntry.PeriodType.DAILY, today, wagered, won, isWin);
        updatePeriodEntry(userId, username, LeaderboardEntry.PeriodType.WEEKLY, weekStart, wagered, won, isWin);
        updatePeriodEntry(userId, username, LeaderboardEntry.PeriodType.MONTHLY, monthStart, wagered, won, isWin);
        updatePeriodEntry(userId, username, LeaderboardEntry.PeriodType.ALL_TIME, ALL_TIME_DATE, wagered, won, isWin);
    }

    /**
     * Update a specific period entry
     */
    @Transactional
    public void updatePeriodEntry(
        String userId,
        String username,
        LeaderboardEntry.PeriodType periodType,
        LocalDate periodDate,
        BigDecimal wagered,
        BigDecimal won,
        boolean isWin
    ) {
        LeaderboardEntry entry = leaderboardRepository
            .findByUserIdAndPeriodTypeAndPeriodDate(userId, periodType, periodDate)
            .orElse(LeaderboardEntry.builder()
                .userId(userId)
                .username(username)
                .periodType(periodType)
                .periodDate(periodDate)
                .totalWagered(BigDecimal.ZERO)
                .totalWon(BigDecimal.ZERO)
                .netProfit(BigDecimal.ZERO)
                .gamesPlayed(0L)
                .gamesWon(0L)
                .score(BigDecimal.ZERO)
                .build());

        // Update metrics
        entry.setTotalWagered(entry.getTotalWagered().add(wagered));
        entry.setTotalWon(entry.getTotalWon().add(won));
        entry.setNetProfit(entry.getTotalWon().subtract(entry.getTotalWagered()));
        entry.setGamesPlayed(entry.getGamesPlayed() + 1);
        if (isWin) {
            entry.setGamesWon(entry.getGamesWon() + 1);
        }

        // Calculate score (using net profit as main metric)
        entry.setScore(entry.getNetProfit());

        leaderboardRepository.save(entry);
    }

    /**
     * Get leaderboard for a period
     */
    public List<LeaderboardEntry> getLeaderboard(
        LeaderboardEntry.PeriodType periodType,
        int limit
    ) {
        LocalDate periodDate = getPeriodDate(periodType);

        List<LeaderboardEntry> entries = leaderboardRepository.findTopByPeriod(
            periodType,
            periodDate,
            PageRequest.of(0, limit)
        );

        // Assign ranks
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank(i + 1);
        }

        return entries;
    }

    /**
     * Get user's rank in a period
     */
    public Optional<LeaderboardEntry> getUserRank(
        String userId,
        LeaderboardEntry.PeriodType periodType
    ) {
        LocalDate periodDate = getPeriodDate(periodType);

        Optional<LeaderboardEntry> entry = leaderboardRepository
            .findByUserIdAndPeriodTypeAndPeriodDate(userId, periodType, periodDate);

        if (entry.isPresent()) {
            LeaderboardEntry e = entry.get();
            Long rank = leaderboardRepository.findRankByScore(periodType, periodDate, e.getScore());
            e.setRank(rank.intValue());
        }

        return entry;
    }

    /**
     * Get user's position in all leaderboards
     */
    public LeaderboardStats getUserLeaderboardStats(String userId) {
        Optional<LeaderboardEntry> daily = getUserRank(userId, LeaderboardEntry.PeriodType.DAILY);
        Optional<LeaderboardEntry> weekly = getUserRank(userId, LeaderboardEntry.PeriodType.WEEKLY);
        Optional<LeaderboardEntry> monthly = getUserRank(userId, LeaderboardEntry.PeriodType.MONTHLY);
        Optional<LeaderboardEntry> allTime = getUserRank(userId, LeaderboardEntry.PeriodType.ALL_TIME);

        return LeaderboardStats.builder()
            .dailyRank(daily.map(LeaderboardEntry::getRank).orElse(null))
            .weeklyRank(weekly.map(LeaderboardEntry::getRank).orElse(null))
            .monthlyRank(monthly.map(LeaderboardEntry::getRank).orElse(null))
            .allTimeRank(allTime.map(LeaderboardEntry::getRank).orElse(null))
            .dailyScore(daily.map(LeaderboardEntry::getScore).orElse(BigDecimal.ZERO))
            .weeklyScore(weekly.map(LeaderboardEntry::getScore).orElse(BigDecimal.ZERO))
            .monthlyScore(monthly.map(LeaderboardEntry::getScore).orElse(BigDecimal.ZERO))
            .allTimeScore(allTime.map(LeaderboardEntry::getScore).orElse(BigDecimal.ZERO))
            .build();
    }

    /**
     * Get period date for a period type
     */
    private LocalDate getPeriodDate(LeaderboardEntry.PeriodType periodType) {
        LocalDate today = LocalDate.now();

        return switch (periodType) {
            case DAILY -> today;
            case WEEKLY -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTHLY -> today.with(TemporalAdjusters.firstDayOfMonth());
            case ALL_TIME -> ALL_TIME_DATE;
        };
    }

    /**
     * Reset leaderboard for a period (scheduled task)
     */
    @Transactional
    public void resetLeaderboard(LeaderboardEntry.PeriodType periodType) {
        LocalDate periodDate = getPeriodDate(periodType);

        leaderboardRepository.deleteByPeriodTypeAndPeriodDate(periodType, periodDate);

        log.info("Reset {} leaderboard for period {}", periodType, periodDate);
    }

    @lombok.Data
    @lombok.Builder
    public static class LeaderboardStats {
        private Integer dailyRank;
        private Integer weeklyRank;
        private Integer monthlyRank;
        private Integer allTimeRank;
        private BigDecimal dailyScore;
        private BigDecimal weeklyScore;
        private BigDecimal monthlyScore;
        private BigDecimal allTimeScore;
    }
}
