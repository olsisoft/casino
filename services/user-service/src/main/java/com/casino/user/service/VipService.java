package com.casino.user.service;

import com.casino.user.entity.UserVipStatus;
import com.casino.user.entity.VipTier;
import com.casino.user.exception.VipException;
import com.casino.user.repository.UserVipStatusRepository;
import com.casino.user.repository.VipTierRepository;
import lombok.Builder;
import lombok.Data;
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
public class VipService {

    private final VipTierRepository vipTierRepository;
    private final UserVipStatusRepository userVipStatusRepository;

    /**
     * Get all VIP tiers
     */
    public List<VipTier> getAllTiers() {
        return vipTierRepository.findByIsActiveTrueOrderByLevelAsc();
    }

    /**
     * Get VIP tier by level
     */
    public VipTier getTierByLevel(Integer level) {
        return vipTierRepository.findByLevel(level)
            .orElseThrow(() -> new VipException("VIP tier not found for level: " + level));
    }

    /**
     * Get user's VIP status
     */
    public UserVipStatus getUserVipStatus(String userId) {
        return userVipStatusRepository.findByUserId(userId)
            .orElseThrow(() -> new VipException("VIP status not found for user: " + userId));
    }

    /**
     * Initialize VIP status for new user
     */
    @Transactional
    public UserVipStatus initializeVipStatus(String userId) {
        if (userVipStatusRepository.existsByUserId(userId)) {
            throw new VipException("VIP status already exists for user: " + userId);
        }

        // Get lowest tier (level 1)
        VipTier lowestTier = vipTierRepository.findByLevel(1)
            .orElseThrow(() -> new VipException("Default VIP tier not found"));

        UserVipStatus vipStatus = UserVipStatus.builder()
            .userId(userId)
            .currentTierId(lowestTier.getId())
            .currentTier(lowestTier)
            .totalWagering(BigDecimal.ZERO)
            .totalDeposit(BigDecimal.ZERO)
            .daysActive(0)
            .vipPoints(0L)
            .monthlyPoints(0L)
            .tierAchievedAt(LocalDateTime.now())
            .build();

        return userVipStatusRepository.save(vipStatus);
    }

    /**
     * Update user activity (wagering, deposits, etc.)
     */
    @Transactional
    public UserVipStatus updateActivity(String userId, BigDecimal wageredAmount, BigDecimal depositAmount) {
        UserVipStatus vipStatus = getUserVipStatus(userId);

        // Update totals
        if (wageredAmount != null && wageredAmount.compareTo(BigDecimal.ZERO) > 0) {
            vipStatus.setTotalWagering(vipStatus.getTotalWagering().add(wageredAmount));

            // Add VIP points (1 point per $1 wagered)
            Long pointsToAdd = wageredAmount.longValue();
            vipStatus.addPoints(pointsToAdd);
        }

        if (depositAmount != null && depositAmount.compareTo(BigDecimal.ZERO) > 0) {
            vipStatus.setTotalDeposit(vipStatus.getTotalDeposit().add(depositAmount));
        }

        // Check for tier upgrade
        checkAndUpgradeTier(vipStatus);

        // Reset monthly points if needed
        if (LocalDate.now().isAfter(vipStatus.getMonthlyPointsResetDate())) {
            vipStatus.resetMonthlyPoints();
        }

        return userVipStatusRepository.save(vipStatus);
    }

    /**
     * Check and upgrade tier if requirements met
     */
    @Transactional
    public void checkAndUpgradeTier(UserVipStatus vipStatus) {
        VipTier currentTier = vipStatus.getCurrentTier();

        // Get all tiers above current
        List<VipTier> allTiers = vipTierRepository.findByIsActiveTrueOrderByLevelAsc();

        VipTier newTier = null;
        for (VipTier tier : allTiers) {
            if (tier.getLevel() > currentTier.getLevel()) {
                if (meetsRequirements(vipStatus, tier)) {
                    newTier = tier;
                } else {
                    break; // Stop at first tier we don't qualify for
                }
            }
        }

        if (newTier != null && !newTier.getId().equals(currentTier.getId())) {
            vipStatus.setCurrentTierId(newTier.getId());
            vipStatus.setCurrentTier(newTier);
            vipStatus.setLastTierUpgradeAt(LocalDateTime.now());
            vipStatus.setTierAchievedAt(LocalDateTime.now());

            log.info("User {} upgraded to VIP tier: {}", vipStatus.getUserId(), newTier.getName());
        }

        // Calculate progress to next tier
        calculateProgressToNextTier(vipStatus);
    }

    /**
     * Check if user meets tier requirements
     */
    private boolean meetsRequirements(UserVipStatus vipStatus, VipTier tier) {
        boolean meetsWagering = vipStatus.getTotalWagering().compareTo(tier.getMinWagering()) >= 0;

        boolean meetsDeposit = true;
        if (tier.getMinDeposit() != null) {
            meetsDeposit = vipStatus.getTotalDeposit().compareTo(tier.getMinDeposit()) >= 0;
        }

        boolean meetsDays = true;
        if (tier.getMinDaysActive() != null) {
            meetsDays = vipStatus.getDaysActive() >= tier.getMinDaysActive();
        }

        return meetsWagering && meetsDeposit && meetsDays;
    }

    /**
     * Calculate progress to next tier
     */
    private void calculateProgressToNextTier(UserVipStatus vipStatus) {
        VipTier currentTier = vipStatus.getCurrentTier();

        Optional<VipTier> nextTierOpt = vipTierRepository.findByLevel(currentTier.getLevel() + 1);
        if (nextTierOpt.isEmpty()) {
            vipStatus.setProgressToNextTier(BigDecimal.valueOf(100)); // Max tier reached
            return;
        }

        VipTier nextTier = nextTierOpt.get();

        BigDecimal wageringProgress = vipStatus.getTotalWagering()
            .divide(nextTier.getMinWagering(), 4, java.math.RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));

        if (wageringProgress.compareTo(BigDecimal.valueOf(100)) > 0) {
            wageringProgress = BigDecimal.valueOf(100);
        }

        vipStatus.setProgressToNextTier(wageringProgress.setScale(2, java.math.RoundingMode.HALF_UP));
    }

    /**
     * Claim monthly bonus
     */
    @Transactional
    public ClaimBonusResponse claimMonthlyBonus(String userId) {
        UserVipStatus vipStatus = getUserVipStatus(userId);

        if (!vipStatus.canClaimMonthlyBonus()) {
            throw new VipException("Monthly bonus already claimed this month");
        }

        VipTier tier = vipStatus.getCurrentTier();
        if (tier.getMonthlyBonus() == null || tier.getMonthlyBonus().compareTo(BigDecimal.ZERO) <= 0) {
            throw new VipException("No monthly bonus available for this tier");
        }

        vipStatus.setLastMonthlyBonusClaimedAt(LocalDateTime.now());
        userVipStatusRepository.save(vipStatus);

        log.info("User {} claimed monthly VIP bonus: {}", userId, tier.getMonthlyBonus());

        return ClaimBonusResponse.builder()
            .bonusType("MONTHLY")
            .amount(tier.getMonthlyBonus())
            .tierName(tier.getName())
            .build();
    }

    /**
     * Claim weekly bonus
     */
    @Transactional
    public ClaimBonusResponse claimWeeklyBonus(String userId) {
        UserVipStatus vipStatus = getUserVipStatus(userId);

        if (!vipStatus.canClaimWeeklyBonus()) {
            throw new VipException("Weekly bonus already claimed this week");
        }

        VipTier tier = vipStatus.getCurrentTier();
        if (tier.getWeeklyBonus() == null || tier.getWeeklyBonus().compareTo(BigDecimal.ZERO) <= 0) {
            throw new VipException("No weekly bonus available for this tier");
        }

        vipStatus.setLastWeeklyBonusClaimedAt(LocalDateTime.now());
        userVipStatusRepository.save(vipStatus);

        log.info("User {} claimed weekly VIP bonus: {}", userId, tier.getWeeklyBonus());

        return ClaimBonusResponse.builder()
            .bonusType("WEEKLY")
            .amount(tier.getWeeklyBonus())
            .tierName(tier.getName())
            .build();
    }

    /**
     * Claim birthday bonus
     */
    @Transactional
    public ClaimBonusResponse claimBirthdayBonus(String userId) {
        UserVipStatus vipStatus = getUserVipStatus(userId);

        if (!vipStatus.canClaimBirthdayBonus()) {
            throw new VipException("Birthday bonus already claimed this year");
        }

        VipTier tier = vipStatus.getCurrentTier();
        if (tier.getBirthdayBonus() == null || tier.getBirthdayBonus().compareTo(BigDecimal.ZERO) <= 0) {
            throw new VipException("No birthday bonus available for this tier");
        }

        vipStatus.setLastBirthdayBonusClaimedAt(LocalDateTime.now());
        userVipStatusRepository.save(vipStatus);

        log.info("User {} claimed birthday VIP bonus: {}", userId, tier.getBirthdayBonus());

        return ClaimBonusResponse.builder()
            .bonusType("BIRTHDAY")
            .amount(tier.getBirthdayBonus())
            .tierName(tier.getName())
            .build();
    }

    /**
     * Get VIP benefits for user
     */
    public VipBenefits getUserBenefits(String userId) {
        UserVipStatus vipStatus = getUserVipStatus(userId);
        VipTier tier = vipStatus.getCurrentTier();

        return VipBenefits.builder()
            .tierName(tier.getName())
            .tierLevel(tier.getLevel())
            .cashbackPercentage(tier.getCashbackPercentage())
            .bonusMultiplier(tier.getBonusMultiplier())
            .monthlyBonus(tier.getMonthlyBonus())
            .weeklyBonus(tier.getWeeklyBonus())
            .birthdayBonus(tier.getBirthdayBonus())
            .withdrawalLimitDaily(tier.getWithdrawalLimitDaily())
            .withdrawalLimitMonthly(tier.getWithdrawalLimitMonthly())
            .withdrawalPriorityHours(tier.getWithdrawalPriorityHours())
            .accessToVipGames(tier.getAccessToVipGames())
            .accessToVipTournaments(tier.getAccessToVipTournaments())
            .personalAccountManager(tier.getPersonalAccountManager())
            .exclusivePromotions(tier.getExclusivePromotions())
            .build();
    }

    /**
     * Create VIP tier (admin)
     */
    @Transactional
    public VipTier createTier(VipTier tier) {
        if (vipTierRepository.findByLevel(tier.getLevel()).isPresent()) {
            throw new VipException("VIP tier with level " + tier.getLevel() + " already exists");
        }

        return vipTierRepository.save(tier);
    }

    /**
     * Update VIP tier (admin)
     */
    @Transactional
    public VipTier updateTier(String tierId, VipTier updatedTier) {
        VipTier existing = vipTierRepository.findById(tierId)
            .orElseThrow(() -> new VipException("VIP tier not found"));

        existing.setName(updatedTier.getName());
        existing.setDescription(updatedTier.getDescription());
        existing.setMinWagering(updatedTier.getMinWagering());
        existing.setMinDeposit(updatedTier.getMinDeposit());
        existing.setMinDaysActive(updatedTier.getMinDaysActive());
        existing.setCashbackPercentage(updatedTier.getCashbackPercentage());
        existing.setBonusMultiplier(updatedTier.getBonusMultiplier());
        existing.setMonthlyBonus(updatedTier.getMonthlyBonus());
        existing.setWeeklyBonus(updatedTier.getWeeklyBonus());
        existing.setBirthdayBonus(updatedTier.getBirthdayBonus());
        existing.setWithdrawalLimitDaily(updatedTier.getWithdrawalLimitDaily());
        existing.setWithdrawalLimitMonthly(updatedTier.getWithdrawalLimitMonthly());
        existing.setWithdrawalPriorityHours(updatedTier.getWithdrawalPriorityHours());

        return vipTierRepository.save(existing);
    }

    @Data
    @Builder
    public static class ClaimBonusResponse {
        private String bonusType;
        private BigDecimal amount;
        private String tierName;
    }

    @Data
    @Builder
    public static class VipBenefits {
        private String tierName;
        private Integer tierLevel;
        private BigDecimal cashbackPercentage;
        private BigDecimal bonusMultiplier;
        private BigDecimal monthlyBonus;
        private BigDecimal weeklyBonus;
        private BigDecimal birthdayBonus;
        private BigDecimal withdrawalLimitDaily;
        private BigDecimal withdrawalLimitMonthly;
        private Integer withdrawalPriorityHours;
        private Boolean accessToVipGames;
        private Boolean accessToVipTournaments;
        private Boolean personalAccountManager;
        private Boolean exclusivePromotions;
    }
}
