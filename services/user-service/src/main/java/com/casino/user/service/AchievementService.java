package com.casino.user.service;

import com.casino.user.entity.Achievement;
import com.casino.user.entity.UserAchievement;
import com.casino.user.exception.AchievementException;
import com.casino.user.repository.AchievementRepository;
import com.casino.user.repository.UserAchievementRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;

    /**
     * Get all available achievements
     */
    public List<Achievement> getAllAchievements() {
        return achievementRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    /**
     * Get all visible achievements (non-hidden)
     */
    public List<Achievement> getVisibleAchievements() {
        return achievementRepository.findByIsHiddenFalseAndIsActiveTrueOrderByDisplayOrderAsc();
    }

    /**
     * Get achievements by category
     */
    public List<Achievement> getAchievementsByCategory(Achievement.AchievementCategory category) {
        return achievementRepository.findByCategoryAndIsActiveTrue(category);
    }

    /**
     * Get user's achievements
     */
    public List<UserAchievement> getUserAchievements(String userId) {
        return userAchievementRepository.findByUserId(userId);
    }

    /**
     * Get user's unlocked achievements
     */
    public List<UserAchievement> getUnlockedAchievements(String userId) {
        return userAchievementRepository.findByUserIdAndIsUnlockedTrue(userId);
    }

    /**
     * Get user's in-progress achievements
     */
    public List<UserAchievement> getInProgressAchievements(String userId) {
        return userAchievementRepository.findInProgress(userId);
    }

    /**
     * Get unclaimed rewards
     */
    public List<UserAchievement> getUnclaimedRewards(String userId) {
        return userAchievementRepository.findByUserIdAndIsUnlockedTrueAndIsRewardClaimedFalse(userId);
    }

    /**
     * Get achievement statistics for a user
     */
    public AchievementStats getUserStats(String userId) {
        Long totalUnlocked = userAchievementRepository.countByUserIdAndIsUnlockedTrue(userId);
        Long totalAchievements = achievementRepository.count();

        List<UserAchievement> userAchievements = userAchievementRepository.findByUserId(userId);

        int totalPoints = userAchievements.stream()
            .filter(UserAchievement::getIsUnlocked)
            .filter(ua -> ua.getAchievement() != null)
            .mapToInt(ua -> ua.getAchievement().getRewardPoints() != null ?
                ua.getAchievement().getRewardPoints() : 0)
            .sum();

        BigDecimal completionPercentage = totalAchievements > 0 ?
            BigDecimal.valueOf(totalUnlocked)
                .divide(BigDecimal.valueOf(totalAchievements), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, java.math.RoundingMode.HALF_UP) :
            BigDecimal.ZERO;

        return AchievementStats.builder()
            .totalAchievements(totalAchievements)
            .unlockedAchievements(totalUnlocked)
            .totalPoints(totalPoints)
            .completionPercentage(completionPercentage)
            .build();
    }

    /**
     * Initialize user achievements (called on first login or registration)
     */
    @Transactional
    public void initializeUserAchievements(String userId) {
        List<Achievement> allAchievements = achievementRepository.findByIsActiveTrue();

        for (Achievement achievement : allAchievements) {
            if (!userAchievementRepository.existsByUserIdAndAchievementId(userId, achievement.getId())) {
                UserAchievement userAchievement = UserAchievement.builder()
                    .userId(userId)
                    .achievementId(achievement.getId())
                    .achievement(achievement)
                    .currentAmount(BigDecimal.ZERO)
                    .currentCount(0L)
                    .currentDays(0)
                    .progressPercentage(BigDecimal.ZERO)
                    .isUnlocked(false)
                    .isRewardClaimed(false)
                    .build();

                userAchievementRepository.save(userAchievement);
            }
        }

        log.info("Initialized achievements for user {}", userId);
    }

    /**
     * Track game played - updates relevant achievements
     */
    @Transactional
    public List<UserAchievement> trackGamePlayed(String userId, String gameType) {
        List<UserAchievement> newlyUnlocked = new ArrayList<>();

        // Find achievements related to games played
        List<Achievement> gameAchievements = achievementRepository
            .findByCategoryAndIsActiveTrue(Achievement.AchievementCategory.GAMES);

        for (Achievement achievement : gameAchievements) {
            UserAchievement userAchievement = getOrCreateUserAchievement(userId, achievement);

            if (!userAchievement.getIsUnlocked()) {
                userAchievement.setCurrentCount(userAchievement.getCurrentCount() + 1);
                userAchievement.calculateProgress();

                if (userAchievement.isCriteriaMet()) {
                    userAchievement.unlock();
                    newlyUnlocked.add(userAchievement);
                    log.info("Achievement unlocked: {} for user {}", achievement.getName(), userId);
                }

                userAchievementRepository.save(userAchievement);
            }
        }

        return newlyUnlocked;
    }

    /**
     * Track wagering - updates relevant achievements
     */
    @Transactional
    public List<UserAchievement> trackWagering(String userId, BigDecimal amount) {
        List<UserAchievement> newlyUnlocked = new ArrayList<>();

        List<Achievement> wageringAchievements = achievementRepository
            .findByCategoryAndIsActiveTrue(Achievement.AchievementCategory.WAGERING);

        for (Achievement achievement : wageringAchievements) {
            UserAchievement userAchievement = getOrCreateUserAchievement(userId, achievement);

            if (!userAchievement.getIsUnlocked()) {
                userAchievement.setCurrentAmount(userAchievement.getCurrentAmount().add(amount));
                userAchievement.calculateProgress();

                if (userAchievement.isCriteriaMet()) {
                    userAchievement.unlock();
                    newlyUnlocked.add(userAchievement);
                    log.info("Achievement unlocked: {} for user {}", achievement.getName(), userId);
                }

                userAchievementRepository.save(userAchievement);
            }
        }

        return newlyUnlocked;
    }

    /**
     * Track winning - updates relevant achievements
     */
    @Transactional
    public List<UserAchievement> trackWinning(String userId, BigDecimal amount) {
        List<UserAchievement> newlyUnlocked = new ArrayList<>();

        List<Achievement> winningAchievements = achievementRepository
            .findByCategoryAndIsActiveTrue(Achievement.AchievementCategory.WINNING);

        for (Achievement achievement : winningAchievements) {
            UserAchievement userAchievement = getOrCreateUserAchievement(userId, achievement);

            if (!userAchievement.getIsUnlocked()) {
                userAchievement.setCurrentAmount(userAchievement.getCurrentAmount().add(amount));
                userAchievement.setCurrentCount(userAchievement.getCurrentCount() + 1);
                userAchievement.calculateProgress();

                if (userAchievement.isCriteriaMet()) {
                    userAchievement.unlock();
                    newlyUnlocked.add(userAchievement);
                    log.info("Achievement unlocked: {} for user {}", achievement.getName(), userId);
                }

                userAchievementRepository.save(userAchievement);
            }
        }

        return newlyUnlocked;
    }

    /**
     * Track streak - updates relevant achievements
     */
    @Transactional
    public List<UserAchievement> trackStreak(String userId, String achievementCode, int days) {
        List<UserAchievement> newlyUnlocked = new ArrayList<>();

        Optional<Achievement> achievementOpt = achievementRepository.findByCode(achievementCode);
        if (achievementOpt.isEmpty()) {
            return newlyUnlocked;
        }

        Achievement achievement = achievementOpt.get();
        UserAchievement userAchievement = getOrCreateUserAchievement(userId, achievement);

        if (!userAchievement.getIsUnlocked()) {
            userAchievement.setCurrentDays(days);
            userAchievement.calculateProgress();

            if (userAchievement.isCriteriaMet()) {
                userAchievement.unlock();
                newlyUnlocked.add(userAchievement);
                log.info("Achievement unlocked: {} for user {}", achievement.getName(), userId);
            }

            userAchievementRepository.save(userAchievement);
        }

        return newlyUnlocked;
    }

    /**
     * Track social activity - updates relevant achievements
     */
    @Transactional
    public List<UserAchievement> trackSocialActivity(String userId, String activityType, int count) {
        List<UserAchievement> newlyUnlocked = new ArrayList<>();

        List<Achievement> socialAchievements = achievementRepository
            .findByCategoryAndIsActiveTrue(Achievement.AchievementCategory.SOCIAL);

        for (Achievement achievement : socialAchievements) {
            // Match achievement code with activity type
            if (achievement.getCode().contains(activityType.toUpperCase())) {
                UserAchievement userAchievement = getOrCreateUserAchievement(userId, achievement);

                if (!userAchievement.getIsUnlocked()) {
                    userAchievement.setCurrentCount((long) count);
                    userAchievement.calculateProgress();

                    if (userAchievement.isCriteriaMet()) {
                        userAchievement.unlock();
                        newlyUnlocked.add(userAchievement);
                        log.info("Achievement unlocked: {} for user {}", achievement.getName(), userId);
                    }

                    userAchievementRepository.save(userAchievement);
                }
            }
        }

        return newlyUnlocked;
    }

    /**
     * Claim achievement reward
     */
    @Transactional
    public ClaimRewardResponse claimReward(String userId, String achievementId) {
        UserAchievement userAchievement = userAchievementRepository
            .findByUserIdAndAchievementId(userId, achievementId)
            .orElseThrow(() -> new AchievementException("User achievement not found"));

        if (!userAchievement.getIsUnlocked()) {
            throw new AchievementException("Achievement not yet unlocked");
        }

        if (userAchievement.getIsRewardClaimed()) {
            throw new AchievementException("Reward already claimed");
        }

        userAchievement.claimReward();
        userAchievementRepository.save(userAchievement);

        Achievement achievement = userAchievement.getAchievement();

        log.info("User {} claimed reward for achievement {}", userId, achievement.getName());

        return ClaimRewardResponse.builder()
            .achievementName(achievement.getName())
            .rewardAmount(achievement.getRewardAmount())
            .rewardPoints(achievement.getRewardPoints())
            .build();
    }

    /**
     * Get or create user achievement
     */
    private UserAchievement getOrCreateUserAchievement(String userId, Achievement achievement) {
        return userAchievementRepository
            .findByUserIdAndAchievementId(userId, achievement.getId())
            .orElseGet(() -> {
                UserAchievement newUserAchievement = UserAchievement.builder()
                    .userId(userId)
                    .achievementId(achievement.getId())
                    .achievement(achievement)
                    .currentAmount(BigDecimal.ZERO)
                    .currentCount(0L)
                    .currentDays(0)
                    .progressPercentage(BigDecimal.ZERO)
                    .isUnlocked(false)
                    .isRewardClaimed(false)
                    .build();
                return userAchievementRepository.save(newUserAchievement);
            });
    }

    /**
     * Create a new achievement (admin)
     */
    @Transactional
    public Achievement createAchievement(Achievement achievement) {
        if (achievementRepository.findByCode(achievement.getCode()).isPresent()) {
            throw new AchievementException("Achievement with code " + achievement.getCode() + " already exists");
        }

        return achievementRepository.save(achievement);
    }

    /**
     * Update achievement (admin)
     */
    @Transactional
    public Achievement updateAchievement(String achievementId, Achievement updatedAchievement) {
        Achievement existing = achievementRepository.findById(achievementId)
            .orElseThrow(() -> new AchievementException("Achievement not found"));

        existing.setName(updatedAchievement.getName());
        existing.setDescription(updatedAchievement.getDescription());
        existing.setCategory(updatedAchievement.getCategory());
        existing.setTier(updatedAchievement.getTier());
        existing.setType(updatedAchievement.getType());
        existing.setIconUrl(updatedAchievement.getIconUrl());
        existing.setTargetAmount(updatedAchievement.getTargetAmount());
        existing.setTargetCount(updatedAchievement.getTargetCount());
        existing.setTargetDays(updatedAchievement.getTargetDays());
        existing.setRewardAmount(updatedAchievement.getRewardAmount());
        existing.setRewardPoints(updatedAchievement.getRewardPoints());
        existing.setIsActive(updatedAchievement.getIsActive());
        existing.setIsHidden(updatedAchievement.getIsHidden());
        existing.setDisplayOrder(updatedAchievement.getDisplayOrder());

        return achievementRepository.save(existing);
    }

    @Data
    @Builder
    public static class AchievementStats {
        private Long totalAchievements;
        private Long unlockedAchievements;
        private Integer totalPoints;
        private BigDecimal completionPercentage;
    }

    @Data
    @Builder
    public static class ClaimRewardResponse {
        private String achievementName;
        private BigDecimal rewardAmount;
        private Integer rewardPoints;
    }
}
