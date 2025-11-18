package com.casino.user.service;

import com.casino.user.entity.DailyReward;
import com.casino.user.exception.DailyRewardException;
import com.casino.user.repository.DailyRewardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyRewardService {

    private final DailyRewardRepository dailyRewardRepository;

    // Reward amounts by streak day
    private static final BigDecimal[] STREAK_REWARDS = {
        new BigDecimal("1.00"),   // Day 1
        new BigDecimal("2.00"),   // Day 2
        new BigDecimal("3.00"),   // Day 3
        new BigDecimal("5.00"),   // Day 4
        new BigDecimal("10.00"),  // Day 5
        new BigDecimal("15.00"),  // Day 6
        new BigDecimal("25.00"),  // Day 7
        new BigDecimal("50.00")   // Day 8+
    };

    /**
     * Get today's daily reward for a user
     */
    public Optional<DailyReward> getTodayReward(String userId) {
        return dailyRewardRepository.findByUserIdAndRewardDate(userId, LocalDate.now());
    }

    /**
     * Get user's daily reward history
     */
    public List<DailyReward> getRewardHistory(String userId) {
        return dailyRewardRepository.findByUserIdOrderByRewardDateDesc(userId);
    }

    /**
     * Get current streak for user
     */
    public int getCurrentStreak(String userId) {
        Optional<DailyReward> latestReward = dailyRewardRepository.findLatestByUserId(userId);

        if (latestReward.isEmpty()) {
            return 0;
        }

        DailyReward latest = latestReward.get();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // If latest reward is from today or yesterday, streak is active
        if (latest.getRewardDate().equals(today) || latest.getRewardDate().equals(yesterday)) {
            return latest.getCurrentStreak();
        }

        // Streak broken
        return 0;
    }

    /**
     * Check if user can claim daily reward today
     */
    public boolean canClaimToday(String userId) {
        Optional<DailyReward> todayReward = getTodayReward(userId);

        if (todayReward.isEmpty()) {
            return true; // Can claim, not claimed yet
        }

        return !todayReward.get().getClaimed(); // Can claim if not yet claimed
    }

    /**
     * Claim daily reward
     */
    @Transactional
    public DailyReward claimDailyReward(String userId) {
        LocalDate today = LocalDate.now();

        // Check if already claimed today
        Optional<DailyReward> existingReward = dailyRewardRepository.findByUserIdAndRewardDate(userId, today);
        if (existingReward.isPresent() && existingReward.get().getClaimed()) {
            throw new DailyRewardException("Daily reward already claimed today");
        }

        // Calculate current streak
        int currentStreak = getCurrentStreak(userId);

        // If there's a reward from yesterday, continue streak
        Optional<DailyReward> yesterday = dailyRewardRepository.findByUserIdAndRewardDate(
            userId, today.minusDays(1)
        );

        if (yesterday.isPresent() && yesterday.get().getClaimed()) {
            currentStreak = yesterday.get().getCurrentStreak() + 1;
        } else if (existingReward.isEmpty()) {
            // New streak starts at 1
            currentStreak = 1;
        }

        // Calculate reward amount based on streak
        BigDecimal rewardAmount = calculateRewardAmount(currentStreak);

        // Get max streak
        Optional<Integer> maxStreak = dailyRewardRepository.findMaxStreakByUserId(userId);
        int newMaxStreak = Math.max(currentStreak, maxStreak.orElse(0));

        DailyReward dailyReward;

        if (existingReward.isPresent()) {
            // Update existing
            dailyReward = existingReward.get();
            dailyReward.setClaimed(true);
            dailyReward.setClaimedAt(LocalDateTime.now());
            dailyReward.setCurrentStreak(currentStreak);
            dailyReward.setMaxStreak(newMaxStreak);
            dailyReward.setRewardAmount(rewardAmount);
        } else {
            // Create new
            dailyReward = DailyReward.builder()
                .userId(userId)
                .rewardDate(today)
                .currentStreak(currentStreak)
                .maxStreak(newMaxStreak)
                .rewardAmount(rewardAmount)
                .rewardType(DailyReward.RewardType.COINS)
                .claimed(true)
                .claimedAt(LocalDateTime.now())
                .build();
        }

        dailyReward = dailyRewardRepository.save(dailyReward);

        log.info("User {} claimed daily reward: day {}, amount {}", userId, currentStreak, rewardAmount);

        return dailyReward;
    }

    /**
     * Calculate reward amount based on streak day
     */
    private BigDecimal calculateRewardAmount(int streakDay) {
        if (streakDay <= 0) {
            return STREAK_REWARDS[0];
        }

        int index = Math.min(streakDay - 1, STREAK_REWARDS.length - 1);
        return STREAK_REWARDS[index];
    }

    /**
     * Get streak reward calendar (7 days)
     */
    public List<StreakRewardInfo> getStreakRewardCalendar() {
        return List.of(
            new StreakRewardInfo(1, STREAK_REWARDS[0], "Day 1"),
            new StreakRewardInfo(2, STREAK_REWARDS[1], "Day 2"),
            new StreakRewardInfo(3, STREAK_REWARDS[2], "Day 3"),
            new StreakRewardInfo(4, STREAK_REWARDS[3], "Day 4"),
            new StreakRewardInfo(5, STREAK_REWARDS[4], "Day 5"),
            new StreakRewardInfo(6, STREAK_REWARDS[5], "Day 6"),
            new StreakRewardInfo(7, STREAK_REWARDS[6], "Day 7"),
            new StreakRewardInfo(8, STREAK_REWARDS[7], "Day 8+")
        );
    }

    /**
     * Get user's daily reward statistics
     */
    public DailyRewardStats getUserStats(String userId) {
        int currentStreak = getCurrentStreak(userId);
        Optional<Integer> maxStreak = dailyRewardRepository.findMaxStreakByUserId(userId);
        Long totalClaimed = dailyRewardRepository.countClaimedByUserId(userId);
        boolean canClaimToday = canClaimToday(userId);
        BigDecimal nextRewardAmount = calculateRewardAmount(currentStreak + 1);

        return DailyRewardStats.builder()
            .currentStreak(currentStreak)
            .maxStreak(maxStreak.orElse(0))
            .totalDaysClaimed(totalClaimed.intValue())
            .canClaimToday(canClaimToday)
            .nextRewardAmount(nextRewardAmount)
            .build();
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class StreakRewardInfo {
        private int day;
        private BigDecimal amount;
        private String label;
    }

    @lombok.Data
    @lombok.Builder
    public static class DailyRewardStats {
        private Integer currentStreak;
        private Integer maxStreak;
        private Integer totalDaysClaimed;
        private Boolean canClaimToday;
        private BigDecimal nextRewardAmount;
    }
}
