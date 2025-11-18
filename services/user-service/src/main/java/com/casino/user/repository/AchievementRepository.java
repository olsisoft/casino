package com.casino.user.repository;

import com.casino.user.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, String> {

    Optional<Achievement> findByCode(String code);

    List<Achievement> findByIsActiveTrue();

    List<Achievement> findByCategoryAndIsActiveTrue(Achievement.AchievementCategory category);

    List<Achievement> findByTierAndIsActiveTrue(Achievement.AchievementTier tier);

    List<Achievement> findByIsActiveTrueOrderByDisplayOrderAsc();

    List<Achievement> findByIsHiddenFalseAndIsActiveTrueOrderByDisplayOrderAsc();
}
