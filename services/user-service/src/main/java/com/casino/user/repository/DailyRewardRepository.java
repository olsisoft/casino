package com.casino.user.repository;

import com.casino.user.entity.DailyReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyRewardRepository extends JpaRepository<DailyReward, String> {

    Optional<DailyReward> findByUserIdAndRewardDate(String userId, LocalDate rewardDate);

    List<DailyReward> findByUserIdOrderByRewardDateDesc(String userId);

    @Query("SELECT d FROM DailyReward d WHERE d.userId = :userId ORDER BY d.rewardDate DESC LIMIT 1")
    Optional<DailyReward> findLatestByUserId(String userId);

    @Query("SELECT d FROM DailyReward d WHERE d.userId = :userId AND d.rewardDate >= :startDate ORDER BY d.rewardDate ASC")
    List<DailyReward> findByUserIdAndRewardDateAfter(String userId, LocalDate startDate);

    @Query("SELECT MAX(d.currentStreak) FROM DailyReward d WHERE d.userId = :userId")
    Optional<Integer> findMaxStreakByUserId(String userId);

    boolean existsByUserIdAndRewardDate(String userId, LocalDate rewardDate);

    @Query("SELECT COUNT(d) FROM DailyReward d WHERE d.userId = :userId AND d.claimed = true")
    Long countClaimedByUserId(String userId);
}
