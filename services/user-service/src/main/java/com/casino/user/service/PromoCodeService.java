package com.casino.user.service;

import com.casino.user.entity.Bonus;
import com.casino.user.entity.PromoCode;
import com.casino.user.entity.User;
import com.casino.user.exception.PromoCodeException;
import com.casino.user.repository.BonusRepository;
import com.casino.user.repository.PromoCodeRepository;
import com.casino.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final BonusRepository bonusRepository;
    private final UserRepository userRepository;

    /**
     * Get all active promo codes
     */
    public List<PromoCode> getAllActivePromoCodes() {
        return promoCodeRepository.findValidPromoCodes(LocalDateTime.now());
    }

    /**
     * Get promo code by code
     */
    public PromoCode getPromoCode(String code) {
        return promoCodeRepository.findByCode(code)
            .orElseThrow(() -> new PromoCodeException("Promo code not found"));
    }

    /**
     * Validate and apply promo code
     */
    @Transactional
    public Bonus applyPromoCode(String userId, String code, BigDecimal depositAmount) {
        // Get promo code
        PromoCode promoCode = promoCodeRepository.findByCodeAndActiveTrue(code)
            .orElseThrow(() -> new PromoCodeException("Invalid or inactive promo code"));

        // Validate promo code
        if (!promoCode.isValid()) {
            throw new PromoCodeException("Promo code is expired or has reached usage limit");
        }

        // Get user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new PromoCodeException("User not found"));

        // Check if user can use this promo code
        if (!promoCode.canBeUsedBy(user)) {
            throw new PromoCodeException("You are not eligible for this promo code");
        }

        // Check minimum deposit (if applicable)
        if (promoCode.getMinDepositAmount() != null &&
            depositAmount.compareTo(promoCode.getMinDepositAmount()) < 0) {
            throw new PromoCodeException(
                String.format("Minimum deposit of $%.2f required", promoCode.getMinDepositAmount())
            );
        }

        // Check if user already used this promo code
        long usageCount = bonusRepository.countByUserIdAndBonusType(userId, Bonus.BonusType.PROMO_CODE);
        if (promoCode.getMaxUsesPerUser() > 0 && usageCount >= promoCode.getMaxUsesPerUser()) {
            throw new PromoCodeException("You have already used this promo code");
        }

        // Calculate bonus amount
        BigDecimal bonusAmount = promoCode.calculateBonusAmount(depositAmount);

        if (bonusAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PromoCodeException("Invalid bonus amount");
        }

        // Create bonus
        BigDecimal requiredWager = bonusAmount.multiply(
            BigDecimal.valueOf(promoCode.getWagerMultiplier())
        );

        Bonus bonus = Bonus.builder()
            .userId(userId)
            .bonusType(Bonus.BonusType.PROMO_CODE)
            .status(Bonus.BonusStatus.ACTIVE)
            .amount(bonusAmount)
            .wageredAmount(BigDecimal.ZERO)
            .requiredWagerAmount(requiredWager)
            .wagerMultiplier(promoCode.getWagerMultiplier())
            .title(promoCode.getName())
            .description(promoCode.getDescription())
            .promoCode(code)
            .allowedGameTypes(promoCode.getAllowedGameTypes())
            .activatedAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusDays(30))
            .build();

        bonus = bonusRepository.save(bonus);

        // Increment promo code usage
        promoCodeRepository.incrementUsage(promoCode.getId());

        log.info("Applied promo code {} to user {}. Bonus amount: {}", code, userId, bonusAmount);

        return bonus;
    }

    /**
     * Create a new promo code (admin function)
     */
    @Transactional
    public PromoCode createPromoCode(PromoCode promoCode) {
        // Check if code already exists
        if (promoCodeRepository.existsByCode(promoCode.getCode())) {
            throw new PromoCodeException("Promo code already exists");
        }

        // Validate promo code configuration
        if (promoCode.getBonusAmount() == null && promoCode.getBonusPercentage() == null) {
            throw new PromoCodeException("Must specify either bonus amount or percentage");
        }

        promoCode = promoCodeRepository.save(promoCode);

        log.info("Created promo code: {}", promoCode.getCode());

        return promoCode;
    }

    /**
     * Update promo code (admin function)
     */
    @Transactional
    public PromoCode updatePromoCode(String promoCodeId, PromoCode updates) {
        PromoCode promoCode = promoCodeRepository.findById(promoCodeId)
            .orElseThrow(() -> new PromoCodeException("Promo code not found"));

        // Update fields
        if (updates.getName() != null) promoCode.setName(updates.getName());
        if (updates.getDescription() != null) promoCode.setDescription(updates.getDescription());
        if (updates.getBonusAmount() != null) promoCode.setBonusAmount(updates.getBonusAmount());
        if (updates.getBonusPercentage() != null) promoCode.setBonusPercentage(updates.getBonusPercentage());
        if (updates.getMaxBonusAmount() != null) promoCode.setMaxBonusAmount(updates.getMaxBonusAmount());
        if (updates.getMinDepositAmount() != null) promoCode.setMinDepositAmount(updates.getMinDepositAmount());
        if (updates.getWagerMultiplier() != null) promoCode.setWagerMultiplier(updates.getWagerMultiplier());
        if (updates.getMaxUses() != null) promoCode.setMaxUses(updates.getMaxUses());
        if (updates.getMaxUsesPerUser() != null) promoCode.setMaxUsesPerUser(updates.getMaxUsesPerUser());
        if (updates.getActive() != null) promoCode.setActive(updates.getActive());
        if (updates.getValidFrom() != null) promoCode.setValidFrom(updates.getValidFrom());
        if (updates.getValidUntil() != null) promoCode.setValidUntil(updates.getValidUntil());

        promoCode = promoCodeRepository.save(promoCode);

        log.info("Updated promo code: {}", promoCode.getCode());

        return promoCode;
    }

    /**
     * Deactivate promo code (admin function)
     */
    @Transactional
    public void deactivatePromoCode(String promoCodeId) {
        PromoCode promoCode = promoCodeRepository.findById(promoCodeId)
            .orElseThrow(() -> new PromoCodeException("Promo code not found"));

        promoCode.setActive(false);
        promoCodeRepository.save(promoCode);

        log.info("Deactivated promo code: {}", promoCode.getCode());
    }

    /**
     * Deactivate expired promo codes (scheduled task)
     */
    @Transactional
    public int deactivateExpiredPromoCodes() {
        int deactivatedCount = promoCodeRepository.deactivateExpiredCodes(LocalDateTime.now());

        if (deactivatedCount > 0) {
            log.info("Deactivated {} expired promo codes", deactivatedCount);
        }

        return deactivatedCount;
    }

    /**
     * Get promo code usage statistics
     */
    public PromoCodeStats getPromoCodeStats(String promoCodeId) {
        PromoCode promoCode = promoCodeRepository.findById(promoCodeId)
            .orElseThrow(() -> new PromoCodeException("Promo code not found"));

        int remainingUses = promoCode.getMaxUses() > 0 ?
            promoCode.getMaxUses() - promoCode.getCurrentUses() : -1; // -1 = unlimited

        double usageRate = promoCode.getMaxUses() > 0 ?
            (double) promoCode.getCurrentUses() / promoCode.getMaxUses() * 100 : 0;

        return PromoCodeStats.builder()
            .code(promoCode.getCode())
            .totalUses(promoCode.getCurrentUses())
            .remainingUses(remainingUses)
            .usageRate(usageRate)
            .isValid(promoCode.isValid())
            .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class PromoCodeStats {
        private String code;
        private Integer totalUses;
        private Integer remainingUses; // -1 = unlimited
        private Double usageRate; // percentage
        private Boolean isValid;
    }
}
