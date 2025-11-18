package com.casino.user.repository;

import com.casino.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {

    @Modifying
    @Query("UPDATE UserProfile p SET p.xp = p.xp + :xp, p.level = :level WHERE p.userId = :userId")
    void updateXpAndLevel(@Param("userId") String userId, @Param("xp") Long xp, @Param("level") Integer level);

    @Modifying
    @Query("UPDATE UserProfile p SET p.gamesPlayed = p.gamesPlayed + 1 WHERE p.userId = :userId")
    void incrementGamesPlayed(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE UserProfile p SET p.totalWagered = p.totalWagered + :amount WHERE p.userId = :userId")
    void addToTotalWagered(@Param("userId") String userId, @Param("amount") Long amount);

    @Modifying
    @Query("UPDATE UserProfile p SET p.totalWon = p.totalWon + :amount WHERE p.userId = :userId")
    void addToTotalWon(@Param("userId") String userId, @Param("amount") Long amount);

    @Modifying
    @Query("UPDATE UserProfile p SET p.currentStreak = :streak WHERE p.userId = :userId")
    void updateCurrentStreak(@Param("userId") String userId, @Param("streak") Integer streak);

    @Modifying
    @Query("UPDATE UserProfile p SET p.bestStreak = :streak WHERE p.userId = :userId AND p.bestStreak < :streak")
    void updateBestStreak(@Param("userId") String userId, @Param("streak") Integer streak);
}
