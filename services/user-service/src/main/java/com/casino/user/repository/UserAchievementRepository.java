package com.casino.user.repository;

import com.casino.user.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, String> {

    Optional<UserAchievement> findByUserIdAndAchievementId(String userId, String achievementId);

    List<UserAchievement> findByUserId(String userId);

    List<UserAchievement> findByUserIdAndIsUnlockedTrue(String userId);

    List<UserAchievement> findByUserIdAndIsUnlockedFalse(String userId);

    List<UserAchievement> findByUserIdAndIsUnlockedTrueAndIsRewardClaimedFalse(String userId);

    Long countByUserIdAndIsUnlockedTrue(String userId);

    @Query("SELECT ua FROM UserAchievement ua WHERE ua.userId = :userId AND ua.isUnlocked = true ORDER BY ua.unlockedAt DESC")
    List<UserAchievement> findRecentUnlocked(String userId);

    @Query("SELECT ua FROM UserAchievement ua WHERE ua.userId = :userId AND ua.progressPercentage > 0 AND ua.isUnlocked = false ORDER BY ua.progressPercentage DESC")
    List<UserAchievement> findInProgress(String userId);

    boolean existsByUserIdAndAchievementId(String userId, String achievementId);
}
