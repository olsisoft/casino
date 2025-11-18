package com.casino.user.service;

import com.casino.user.repository.*;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final KycVerificationRepository kycVerificationRepository;
    private final AmlAlertRepository amlAlertRepository;
    private final AffiliateRepository affiliateRepository;
    private final BonusRepository bonusRepository;

    /**
     * Get dashboard overview statistics
     */
    public DashboardOverview getOverview() {
        // User statistics
        Long totalUsers = userRepository.count();
        Long activeUsers = userRepository.countByIsActiveTrue();
        Long newUsersToday = userRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(1));
        Long newUsersThisMonth = userRepository.countByCreatedAtAfter(
            LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
        );

        // KYC statistics
        Long pendingKyc = kycVerificationRepository.countByStatus(
            com.casino.user.entity.KycVerification.KycStatus.PENDING
        );
        Long verifiedUsers = kycVerificationRepository.countByStatus(
            com.casino.user.entity.KycVerification.KycStatus.APPROVED
        );

        // AML statistics
        Long criticalAlerts = amlAlertRepository.countBySeverity(
            com.casino.user.entity.AmlAlert.AlertSeverity.CRITICAL
        );
        Long newAlerts = amlAlertRepository.countByStatus(
            com.casino.user.entity.AmlAlert.AlertStatus.NEW
        );

        // Affiliate statistics
        Long activeAffiliates = affiliateRepository.countByStatus(
            com.casino.user.entity.Affiliate.AffiliateStatus.ACTIVE
        );
        Long pendingAffiliates = affiliateRepository.countByStatus(
            com.casino.user.entity.Affiliate.AffiliateStatus.PENDING
        );

        // Bonus statistics
        Long activeBonuses = bonusRepository.countByStatus(
            com.casino.user.entity.Bonus.BonusStatus.ACTIVE
        );

        return DashboardOverview.builder()
            .totalUsers(totalUsers)
            .activeUsers(activeUsers)
            .newUsersToday(newUsersToday)
            .newUsersThisMonth(newUsersThisMonth)
            .pendingKyc(pendingKyc)
            .verifiedUsers(verifiedUsers)
            .criticalAlerts(criticalAlerts)
            .newAlerts(newAlerts)
            .activeAffiliates(activeAffiliates)
            .pendingAffiliates(pendingAffiliates)
            .activeBonuses(activeBonuses)
            .build();
    }

    /**
     * Get user statistics
     */
    public UserStatistics getUserStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        Long newUsers = userRepository.countByCreatedAtBetween(start, end);
        Long activeUsers = userRepository.countByIsActiveTrueAndCreatedAtBetween(start, end);

        // Calculate retention (simplified)
        Long totalUsers = userRepository.count();
        BigDecimal retentionRate = totalUsers > 0 ?
            BigDecimal.valueOf(activeUsers)
                .divide(BigDecimal.valueOf(totalUsers), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)) :
            BigDecimal.ZERO;

        return UserStatistics.builder()
            .totalUsers(totalUsers)
            .newUsers(newUsers)
            .activeUsers(activeUsers)
            .retentionRate(retentionRate)
            .periodStart(startDate)
            .periodEnd(endDate)
            .build();
    }

    /**
     * Get financial overview (placeholder - would integrate with payment service)
     */
    public FinancialOverview getFinancialOverview(LocalDate startDate, LocalDate endDate) {
        // In production, this would query transaction/payment tables
        // For now, returning placeholder data structure

        return FinancialOverview.builder()
            .totalDeposits(BigDecimal.ZERO)
            .totalWithdrawals(BigDecimal.ZERO)
            .totalWagered(BigDecimal.ZERO)
            .totalWon(BigDecimal.ZERO)
            .grossGamingRevenue(BigDecimal.ZERO)
            .netGamingRevenue(BigDecimal.ZERO)
            .totalBonusesIssued(BigDecimal.ZERO)
            .totalAffiliateCommissions(BigDecimal.ZERO)
            .periodStart(startDate)
            .periodEnd(endDate)
            .build();
    }

    /**
     * Get game statistics (placeholder - would integrate with game service)
     */
    public GameStatistics getGameStatistics(LocalDate startDate, LocalDate endDate) {
        return GameStatistics.builder()
            .totalGamesPlayed(0L)
            .totalBetsPlaced(0L)
            .averageBetSize(BigDecimal.ZERO)
            .mostPopularGame("Blackjack")
            .totalRounds(0L)
            .periodStart(startDate)
            .periodEnd(endDate)
            .build();
    }

    /**
     * Get compliance overview
     */
    public ComplianceOverview getComplianceOverview() {
        Long pendingKyc = kycVerificationRepository.countByStatus(
            com.casino.user.entity.KycVerification.KycStatus.PENDING
        );
        Long approvedKyc = kycVerificationRepository.countByStatus(
            com.casino.user.entity.KycVerification.KycStatus.APPROVED
        );
        Long rejectedKyc = kycVerificationRepository.countByStatus(
            com.casino.user.entity.KycVerification.KycStatus.REJECTED
        );

        Long newAlerts = amlAlertRepository.countByStatus(
            com.casino.user.entity.AmlAlert.AlertStatus.NEW
        );
        Long criticalAlerts = amlAlertRepository.countBySeverity(
            com.casino.user.entity.AmlAlert.AlertSeverity.CRITICAL
        );
        Long resolvedAlerts = amlAlertRepository.countByStatus(
            com.casino.user.entity.AmlAlert.AlertStatus.RESOLVED
        );

        return ComplianceOverview.builder()
            .pendingKyc(pendingKyc)
            .approvedKyc(approvedKyc)
            .rejectedKyc(rejectedKyc)
            .newAmlAlerts(newAlerts)
            .criticalAmlAlerts(criticalAlerts)
            .resolvedAmlAlerts(resolvedAlerts)
            .build();
    }

    @Data
    @Builder
    public static class DashboardOverview {
        private Long totalUsers;
        private Long activeUsers;
        private Long newUsersToday;
        private Long newUsersThisMonth;
        private Long pendingKyc;
        private Long verifiedUsers;
        private Long criticalAlerts;
        private Long newAlerts;
        private Long activeAffiliates;
        private Long pendingAffiliates;
        private Long activeBonuses;
    }

    @Data
    @Builder
    public static class UserStatistics {
        private Long totalUsers;
        private Long newUsers;
        private Long activeUsers;
        private BigDecimal retentionRate;
        private LocalDate periodStart;
        private LocalDate periodEnd;
    }

    @Data
    @Builder
    public static class FinancialOverview {
        private BigDecimal totalDeposits;
        private BigDecimal totalWithdrawals;
        private BigDecimal totalWagered;
        private BigDecimal totalWon;
        private BigDecimal grossGamingRevenue;
        private BigDecimal netGamingRevenue;
        private BigDecimal totalBonusesIssued;
        private BigDecimal totalAffiliateCommissions;
        private LocalDate periodStart;
        private LocalDate periodEnd;
    }

    @Data
    @Builder
    public static class GameStatistics {
        private Long totalGamesPlayed;
        private Long totalBetsPlaced;
        private BigDecimal averageBetSize;
        private String mostPopularGame;
        private Long totalRounds;
        private LocalDate periodStart;
        private LocalDate periodEnd;
    }

    @Data
    @Builder
    public static class ComplianceOverview {
        private Long pendingKyc;
        private Long approvedKyc;
        private Long rejectedKyc;
        private Long newAmlAlerts;
        private Long criticalAmlAlerts;
        private Long resolvedAmlAlerts;
    }
}
