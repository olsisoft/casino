package com.casino.user.service;

import com.casino.user.entity.Bonus;
import com.casino.user.entity.User;
import com.casino.user.exception.BonusException;
import com.casino.user.repository.BonusRepository;
import com.casino.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BonusService {

    private final BonusRepository bonusRepository;
    private final UserRepository userRepository;

    // Default configurations
    private static final int DEFAULT_WAGER_MULTIPLIER = 30;
    private static final int BONUS_EXPIRY_DAYS = 30;

    /**
     * Get all bonuses for a user
     */
    public List<Bonus> getUserBonuses(String userId) {
        return bonusRepository.findByUserIdOrderByIssuedAtDesc(userId);
    }

    /**
     * Get active bonuses for a user
     */
    public List<Bonus> getActiveBonuses(String userId) {
        return bonusRepository.findByUserIdAndStatus(userId, Bonus.BonusStatus.ACTIVE);
    }

    /**
     * Get pending bonuses for a user
     */
    public List<Bonus> getPendingBonuses(String userId) {
        return bonusRepository.findByUserIdAndStatus(userId, Bonus.BonusStatus.PENDING);
    }

    /**
     * Issue a welcome bonus to a new user
     */
    @Transactional
    public Bonus issueWelcomeBonus(String userId, BigDecimal depositAmount) {
        // Check if user already has a welcome bonus
        if (bonusRepository.existsByUserIdAndBonusType(userId, Bonus.BonusType.WELCOME_BONUS)) {
            throw new BonusException("User already received welcome bonus");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BonusException("User not found"));

        // Welcome bonus: 100% match up to $500
        BigDecimal bonusPercentage = BigDecimal.valueOf(100); // 100%
        BigDecimal maxBonus = new BigDecimal("500.00");

        BigDecimal bonusAmount = depositAmount
            .multiply(bonusPercentage)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
            .min(maxBonus);

        BigDecimal requiredWager = bonusAmount.multiply(BigDecimal.valueOf(DEFAULT_WAGER_MULTIPLIER));

        Bonus bonus = Bonus.builder()
            .userId(userId)
            .bonusType(Bonus.BonusType.WELCOME_BONUS)
            .status(Bonus.BonusStatus.ACTIVE)
            .amount(bonusAmount)
            .wageredAmount(BigDecimal.ZERO)
            .requiredWagerAmount(requiredWager)
            .wagerMultiplier(DEFAULT_WAGER_MULTIPLIER)
            .title("Welcome Bonus")
            .description("100% match bonus up to $500 on your first deposit")
            .activatedAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusDays(BONUS_EXPIRY_DAYS))
            .build();

        bonus = bonusRepository.save(bonus);

        log.info("Issued welcome bonus {} to user {}: amount={}, requiredWager={}",
            bonus.getId(), userId, bonusAmount, requiredWager);

        return bonus;
    }

    /**
     * Issue a deposit bonus
     */
    @Transactional
    public Bonus issueDepositBonus(
        String userId,
        BigDecimal depositAmount,
        BigDecimal bonusPercentage,
        BigDecimal maxBonusAmount
    ) {
        BigDecimal bonusAmount = depositAmount
            .multiply(bonusPercentage)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
            .min(maxBonusAmount);

        BigDecimal requiredWager = bonusAmount.multiply(BigDecimal.valueOf(DEFAULT_WAGER_MULTIPLIER));

        Bonus bonus = Bonus.builder()
            .userId(userId)
            .bonusType(Bonus.BonusType.DEPOSIT_BONUS)
            .status(Bonus.BonusStatus.ACTIVE)
            .amount(bonusAmount)
            .wageredAmount(BigDecimal.ZERO)
            .requiredWagerAmount(requiredWager)
            .wagerMultiplier(DEFAULT_WAGER_MULTIPLIER)
            .title("Deposit Bonus")
            .description(String.format("%.0f%% match bonus up to $%.2f",
                bonusPercentage, maxBonusAmount))
            .activatedAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusDays(BONUS_EXPIRY_DAYS))
            .build();

        bonus = bonusRepository.save(bonus);

        log.info("Issued deposit bonus {} to user {}: amount={}", bonus.getId(), userId, bonusAmount);

        return bonus;
    }

    /**
     * Issue a no-deposit bonus (free bonus)
     */
    @Transactional
    public Bonus issueNoDepositBonus(String userId, BigDecimal bonusAmount, String title, String description) {
        BigDecimal requiredWager = bonusAmount.multiply(BigDecimal.valueOf(DEFAULT_WAGER_MULTIPLIER));

        Bonus bonus = Bonus.builder()
            .userId(userId)
            .bonusType(Bonus.BonusType.NO_DEPOSIT_BONUS)
            .status(Bonus.BonusStatus.ACTIVE)
            .amount(bonusAmount)
            .wageredAmount(BigDecimal.ZERO)
            .requiredWagerAmount(requiredWager)
            .wagerMultiplier(DEFAULT_WAGER_MULTIPLIER)
            .title(title)
            .description(description)
            .activatedAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusDays(BONUS_EXPIRY_DAYS))
            .build();

        bonus = bonusRepository.save(bonus);

        log.info("Issued no-deposit bonus {} to user {}: amount={}", bonus.getId(), userId, bonusAmount);

        return bonus;
    }

    /**
     * Activate a pending bonus
     */
    @Transactional
    public Bonus activateBonus(String bonusId, String userId) {
        Bonus bonus = bonusRepository.findById(bonusId)
            .orElseThrow(() -> new BonusException("Bonus not found"));

        if (!bonus.getUserId().equals(userId)) {
            throw new BonusException("Bonus does not belong to user");
        }

        if (!bonus.canBeActivated()) {
            throw new BonusException("Bonus cannot be activated");
        }

        bonus.setStatus(Bonus.BonusStatus.ACTIVE);
        bonus.setActivatedAt(LocalDateTime.now());

        bonus = bonusRepository.save(bonus);

        log.info("Activated bonus {} for user {}", bonusId, userId);

        return bonus;
    }

    /**
     * Record wagering for active bonuses
     * This should be called after each bet
     */
    @Transactional
    public void recordWagering(String userId, BigDecimal betAmount) {
        List<Bonus> activeBonuses = getActiveBonuses(userId);

        for (Bonus bonus : activeBonuses) {
            if (bonus.isExpired()) {
                bonus.setStatus(Bonus.BonusStatus.EXPIRED);
                bonusRepository.save(bonus);
                continue;
            }

            // Add wagered amount
            BigDecimal newWageredAmount = bonus.getWageredAmount().add(betAmount);
            bonus.setWageredAmount(newWageredAmount);

            // Check if wagering requirement is met
            if (newWageredAmount.compareTo(bonus.getRequiredWagerAmount()) >= 0) {
                bonus.setStatus(Bonus.BonusStatus.COMPLETED);
                bonus.setCompletedAt(LocalDateTime.now());

                log.info("Bonus {} completed for user {}. Total wagered: {}",
                    bonus.getId(), userId, newWageredAmount);
            }

            bonusRepository.save(bonus);
        }
    }

    /**
     * Cancel a bonus
     */
    @Transactional
    public void cancelBonus(String bonusId, String userId) {
        Bonus bonus = bonusRepository.findById(bonusId)
            .orElseThrow(() -> new BonusException("Bonus not found"));

        if (!bonus.getUserId().equals(userId)) {
            throw new BonusException("Bonus does not belong to user");
        }

        if (bonus.getStatus() == Bonus.BonusStatus.COMPLETED) {
            throw new BonusException("Cannot cancel completed bonus");
        }

        bonus.setStatus(Bonus.BonusStatus.CANCELLED);
        bonus.setCancelledAt(LocalDateTime.now());

        bonusRepository.save(bonus);

        log.info("Cancelled bonus {} for user {}", bonusId, userId);
    }

    /**
     * Forfeit a bonus (e.g., when user withdraws before meeting wagering requirement)
     */
    @Transactional
    public void forfeitBonus(String bonusId, String userId) {
        Bonus bonus = bonusRepository.findById(bonusId)
            .orElseThrow(() -> new BonusException("Bonus not found"));

        if (!bonus.getUserId().equals(userId)) {
            throw new BonusException("Bonus does not belong to user");
        }

        bonus.setStatus(Bonus.BonusStatus.FORFEITED);

        bonusRepository.save(bonus);

        log.info("Forfeited bonus {} for user {}", bonusId, userId);
    }

    /**
     * Get total active bonus balance for a user
     */
    public BigDecimal getTotalActiveBonusBalance(String userId) {
        List<Bonus> activeBonuses = getActiveBonuses(userId);

        return activeBonuses.stream()
            .map(Bonus::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Expire old bonuses (scheduled task)
     */
    @Transactional
    public int expireOldBonuses() {
        int expiredCount = bonusRepository.expireOldBonuses(LocalDateTime.now());

        if (expiredCount > 0) {
            log.info("Expired {} old bonuses", expiredCount);
        }

        return expiredCount;
    }

    /**
     * Check if user has active wagering requirements
     */
    public boolean hasActiveWageringRequirements(String userId) {
        List<Bonus> activeBonuses = getActiveBonuses(userId);
        return !activeBonuses.isEmpty();
    }

    /**
     * Get total remaining wagering requirement
     */
    public BigDecimal getTotalRemainingWagerRequirement(String userId) {
        List<Bonus> activeBonuses = getActiveBonuses(userId);

        return activeBonuses.stream()
            .map(Bonus::getRemainingWagerAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
