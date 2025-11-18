package com.casino.user.service;

import com.casino.user.entity.Affiliate;
import com.casino.user.entity.AffiliateCommission;
import com.casino.user.entity.AffiliateReferral;
import com.casino.user.exception.AffiliateException;
import com.casino.user.repository.AffiliateCommissionRepository;
import com.casino.user.repository.AffiliateReferralRepository;
import com.casino.user.repository.AffiliateRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AffiliateService {

    private final AffiliateRepository affiliateRepository;
    private final AffiliateReferralRepository affiliateReferralRepository;
    private final AffiliateCommissionRepository affiliateCommissionRepository;

    private static final String AFFILIATE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int AFFILIATE_CODE_LENGTH = 8;

    /**
     * Apply to become an affiliate
     */
    @Transactional
    public Affiliate applyForAffiliate(String userId, String websiteUrl, String trafficSource) {
        if (affiliateRepository.existsByUserId(userId)) {
            throw new AffiliateException("User already has an affiliate account");
        }

        String affiliateCode = generateUniqueAffiliateCode();

        Affiliate affiliate = Affiliate.builder()
            .userId(userId)
            .affiliateCode(affiliateCode)
            .status(Affiliate.AffiliateStatus.PENDING)
            .tier(Affiliate.AffiliateTier.STANDARD)
            .commissionPercentage(new BigDecimal("25.00")) // Default 25%
            .cpaAmount(new BigDecimal("50.00")) // Default $50 CPA
            .minimumPayout(new BigDecimal("100.00"))
            .websiteUrl(websiteUrl)
            .trafficSource(trafficSource)
            .isActive(true)
            .build();

        log.info("Affiliate application created for user: {} with code: {}", userId, affiliateCode);

        return affiliateRepository.save(affiliate);
    }

    /**
     * Approve affiliate application (admin)
     */
    @Transactional
    public Affiliate approveAffiliate(String affiliateId) {
        Affiliate affiliate = affiliateRepository.findById(affiliateId)
            .orElseThrow(() -> new AffiliateException("Affiliate not found"));

        affiliate.setStatus(Affiliate.AffiliateStatus.ACTIVE);
        return affiliateRepository.save(affiliate);
    }

    /**
     * Register a referral
     */
    @Transactional
    public AffiliateReferral registerReferral(String affiliateCode, String referredUserId, String ipAddress, String userAgent) {
        Affiliate affiliate = affiliateRepository.findByAffiliateCode(affiliateCode)
            .orElseThrow(() -> new AffiliateException("Invalid affiliate code"));

        if (affiliate.getStatus() != Affiliate.AffiliateStatus.ACTIVE) {
            throw new AffiliateException("Affiliate is not active");
        }

        if (affiliateReferralRepository.existsByReferredUserId(referredUserId)) {
            throw new AffiliateException("User already referred by another affiliate");
        }

        AffiliateReferral referral = AffiliateReferral.builder()
            .affiliateId(affiliate.getId())
            .referredUserId(referredUserId)
            .affiliateCode(affiliateCode)
            .status(AffiliateReferral.ReferralStatus.PENDING)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();

        referral = affiliateReferralRepository.save(referral);

        // Update affiliate stats
        affiliate.setTotalReferrals(affiliate.getTotalReferrals() + 1);
        affiliateRepository.save(affiliate);

        log.info("Referral registered: {} for affiliate: {}", referredUserId, affiliate.getAffiliateCode());

        return referral;
    }

    /**
     * Activate referral (when user makes first deposit)
     */
    @Transactional
    public void activateReferral(String referredUserId, BigDecimal firstDepositAmount) {
        AffiliateReferral referral = affiliateReferralRepository.findByReferredUserId(referredUserId)
            .orElse(null);

        if (referral == null || referral.getStatus() != AffiliateReferral.ReferralStatus.PENDING) {
            return; // No pending referral
        }

        referral.setStatus(AffiliateReferral.ReferralStatus.ACTIVE);
        referral.setFirstDepositAt(LocalDateTime.now());
        referral.setLastActivityAt(LocalDateTime.now());
        affiliateReferralRepository.save(referral);

        // Update affiliate
        Affiliate affiliate = affiliateRepository.findById(referral.getAffiliateId())
            .orElseThrow(() -> new AffiliateException("Affiliate not found"));

        affiliate.setActiveReferrals(affiliate.getActiveReferrals() + 1);

        // Check for tier upgrade
        updateAffiliateTier(affiliate);

        // Process CPA commission if applicable
        if (affiliate.getCpaAmount() != null && affiliate.getCpaAmount().compareTo(BigDecimal.ZERO) > 0) {
            createCpaCommission(affiliate, referral);
        }

        affiliateRepository.save(affiliate);

        log.info("Referral activated: {} for affiliate: {}", referredUserId, affiliate.getAffiliateCode());
    }

    /**
     * Track revenue from referral
     */
    @Transactional
    public void trackReferralRevenue(String referredUserId, BigDecimal revenue) {
        AffiliateReferral referral = affiliateReferralRepository.findByReferredUserId(referredUserId)
            .orElse(null);

        if (referral == null || referral.getStatus() != AffiliateReferral.ReferralStatus.ACTIVE) {
            return;
        }

        referral.setTotalRevenue(referral.getTotalRevenue().add(revenue));
        referral.setLastActivityAt(LocalDateTime.now());
        affiliateReferralRepository.save(referral);

        // Calculate and create commission
        Affiliate affiliate = affiliateRepository.findById(referral.getAffiliateId())
            .orElseThrow(() -> new AffiliateException("Affiliate not found"));

        BigDecimal commission = revenue
            .multiply(affiliate.getCommissionPercentage())
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        createRevenueShareCommission(affiliate, referral, revenue, commission);

        // Update affiliate totals
        affiliate.setLifetimeReferralRevenue(affiliate.getLifetimeReferralRevenue().add(revenue));
        affiliateRepository.save(affiliate);
    }

    /**
     * Create CPA commission
     */
    private void createCpaCommission(Affiliate affiliate, AffiliateReferral referral) {
        AffiliateCommission commission = AffiliateCommission.builder()
            .affiliateId(affiliate.getId())
            .referralId(referral.getId())
            .type(AffiliateCommission.CommissionType.CPA)
            .amount(affiliate.getCpaAmount())
            .status(AffiliateCommission.CommissionStatus.APPROVED)
            .build();

        affiliateCommissionRepository.save(commission);

        // Update affiliate earnings
        affiliate.setTotalEarnings(affiliate.getTotalEarnings().add(affiliate.getCpaAmount()));
        affiliate.setPendingEarnings(affiliate.getPendingEarnings().add(affiliate.getCpaAmount()));

        log.info("CPA commission created: {} for affiliate: {}", affiliate.getCpaAmount(), affiliate.getAffiliateCode());
    }

    /**
     * Create revenue share commission
     */
    private void createRevenueShareCommission(Affiliate affiliate, AffiliateReferral referral, BigDecimal revenue, BigDecimal commission) {
        AffiliateCommission commissionRecord = AffiliateCommission.builder()
            .affiliateId(affiliate.getId())
            .referralId(referral.getId())
            .type(AffiliateCommission.CommissionType.REVENUE_SHARE)
            .amount(commission)
            .referralRevenue(revenue)
            .commissionRate(affiliate.getCommissionPercentage())
            .periodStart(LocalDate.now().withDayOfMonth(1))
            .periodEnd(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()))
            .status(AffiliateCommission.CommissionStatus.APPROVED)
            .build();

        affiliateCommissionRepository.save(commissionRecord);

        // Update referral
        referral.setTotalCommission(referral.getTotalCommission().add(commission));

        // Update affiliate earnings
        affiliate.setTotalEarnings(affiliate.getTotalEarnings().add(commission));
        affiliate.setPendingEarnings(affiliate.getPendingEarnings().add(commission));
    }

    /**
     * Get affiliate dashboard stats
     */
    public AffiliateDashboard getAffiliateDashboard(String userId) {
        Affiliate affiliate = affiliateRepository.findByUserId(userId)
            .orElseThrow(() -> new AffiliateException("Affiliate not found"));

        List<AffiliateReferral> referrals = affiliateReferralRepository.findByAffiliateId(affiliate.getId());

        return AffiliateDashboard.builder()
            .affiliateCode(affiliate.getAffiliateCode())
            .status(affiliate.getStatus())
            .tier(affiliate.getTier())
            .totalReferrals(affiliate.getTotalReferrals())
            .activeReferrals(affiliate.getActiveReferrals())
            .conversionRate(affiliate.getConversionRate())
            .totalEarnings(affiliate.getTotalEarnings())
            .pendingEarnings(affiliate.getPendingEarnings())
            .paidEarnings(affiliate.getPaidEarnings())
            .lifetimeRevenue(affiliate.getLifetimeReferralRevenue())
            .commissionPercentage(affiliate.getCommissionPercentage())
            .cpaAmount(affiliate.getCpaAmount())
            .build();
    }

    /**
     * Request payout
     */
    @Transactional
    public PayoutResponse requestPayout(String userId) {
        Affiliate affiliate = affiliateRepository.findByUserId(userId)
            .orElseThrow(() -> new AffiliateException("Affiliate not found"));

        if (affiliate.getPendingEarnings().compareTo(affiliate.getMinimumPayout()) < 0) {
            throw new AffiliateException("Minimum payout amount not reached: " + affiliate.getMinimumPayout());
        }

        BigDecimal payoutAmount = affiliate.getPendingEarnings();

        // Mark commissions as paid
        List<AffiliateCommission> pendingCommissions = affiliateCommissionRepository
            .findByAffiliateIdAndStatus(affiliate.getId(), AffiliateCommission.CommissionStatus.APPROVED);

        for (AffiliateCommission commission : pendingCommissions) {
            commission.setStatus(AffiliateCommission.CommissionStatus.PAID);
            commission.setPaidAt(LocalDateTime.now());
            affiliateCommissionRepository.save(commission);
        }

        // Update affiliate
        affiliate.setPendingEarnings(BigDecimal.ZERO);
        affiliate.setPaidEarnings(affiliate.getPaidEarnings().add(payoutAmount));
        affiliate.setLastPayoutAt(LocalDateTime.now());
        affiliateRepository.save(affiliate);

        log.info("Payout requested: {} for affiliate: {}", payoutAmount, affiliate.getAffiliateCode());

        return PayoutResponse.builder()
            .amount(payoutAmount)
            .paymentMethod(affiliate.getPaymentMethod())
            .build();
    }

    /**
     * Update affiliate tier based on active referrals
     */
    private void updateAffiliateTier(Affiliate affiliate) {
        Long activeReferrals = affiliate.getActiveReferrals();

        Affiliate.AffiliateTier newTier;
        if (activeReferrals >= 100) {
            newTier = Affiliate.AffiliateTier.PLATINUM;
        } else if (activeReferrals >= 51) {
            newTier = Affiliate.AffiliateTier.GOLD;
        } else if (activeReferrals >= 26) {
            newTier = Affiliate.AffiliateTier.SILVER;
        } else if (activeReferrals >= 11) {
            newTier = Affiliate.AffiliateTier.BRONZE;
        } else {
            newTier = Affiliate.AffiliateTier.STANDARD;
        }

        if (newTier != affiliate.getTier()) {
            affiliate.setTier(newTier);
            log.info("Affiliate {} upgraded to tier: {}", affiliate.getAffiliateCode(), newTier);
        }
    }

    /**
     * Generate unique affiliate code
     */
    private String generateUniqueAffiliateCode() {
        Random random = new Random();
        String code;

        do {
            StringBuilder sb = new StringBuilder(AFFILIATE_CODE_LENGTH);
            for (int i = 0; i < AFFILIATE_CODE_LENGTH; i++) {
                sb.append(AFFILIATE_CODE_CHARS.charAt(random.nextInt(AFFILIATE_CODE_CHARS.length())));
            }
            code = sb.toString();
        } while (affiliateRepository.existsByAffiliateCode(code));

        return code;
    }

    /**
     * Get affiliate by code
     */
    public Affiliate getAffiliateByCode(String code) {
        return affiliateRepository.findByAffiliateCode(code)
            .orElseThrow(() -> new AffiliateException("Affiliate not found"));
    }

    /**
     * Get all referrals for affiliate
     */
    public List<AffiliateReferral> getAffiliateReferrals(String userId) {
        Affiliate affiliate = affiliateRepository.findByUserId(userId)
            .orElseThrow(() -> new AffiliateException("Affiliate not found"));

        return affiliateReferralRepository.findByAffiliateId(affiliate.getId());
    }

    /**
     * Get all commissions for affiliate
     */
    public List<AffiliateCommission> getAffiliateCommissions(String userId) {
        Affiliate affiliate = affiliateRepository.findByUserId(userId)
            .orElseThrow(() -> new AffiliateException("Affiliate not found"));

        return affiliateCommissionRepository.findByAffiliateId(affiliate.getId());
    }

    @Data
    @Builder
    public static class AffiliateDashboard {
        private String affiliateCode;
        private Affiliate.AffiliateStatus status;
        private Affiliate.AffiliateTier tier;
        private Long totalReferrals;
        private Long activeReferrals;
        private BigDecimal conversionRate;
        private BigDecimal totalEarnings;
        private BigDecimal pendingEarnings;
        private BigDecimal paidEarnings;
        private BigDecimal lifetimeRevenue;
        private BigDecimal commissionPercentage;
        private BigDecimal cpaAmount;
    }

    @Data
    @Builder
    public static class PayoutResponse {
        private BigDecimal amount;
        private String paymentMethod;
    }
}
