package com.casino.user.service;

import com.casino.user.entity.CashbackRecord;
import com.casino.user.entity.User;
import com.casino.user.exception.CashbackException;
import com.casino.user.repository.CashbackRepository;
import com.casino.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashbackService {

    private final CashbackRepository cashbackRepository;
    private final UserRepository userRepository;

    // Default cashback percentages by period
    private static final BigDecimal DAILY_CASHBACK_PERCENTAGE = new BigDecimal("5.00"); // 5%
    private static final BigDecimal WEEKLY_CASHBACK_PERCENTAGE = new BigDecimal("10.00"); // 10%
    private static final BigDecimal MONTHLY_CASHBACK_PERCENTAGE = new BigDecimal("15.00"); // 15%

    // Minimum loss required to receive cashback
    private static final BigDecimal MIN_LOSS_FOR_CASHBACK = new BigDecimal("10.00");

    // Cashback expiry
    private static final int CASHBACK_EXPIRY_DAYS = 7;

    /**
     * Get all cashback records for a user
     */
    public List<CashbackRecord> getUserCashbacks(String userId) {
        return cashbackRepository.findByUserIdOrderByPeriodStartDesc(userId);
    }

    /**
     * Get claimable cashbacks for a user
     */
    public List<CashbackRecord> getClaimableCashbacks(String userId) {
        return cashbackRepository.findClaimableByUserId(userId, LocalDateTime.now());
    }

    /**
     * Calculate cashback for a period
     */
    @Transactional
    public CashbackRecord calculateCashback(
        String userId,
        LocalDate periodStart,
        LocalDate periodEnd,
        CashbackRecord.CashbackPeriod cashbackPeriod,
        BigDecimal totalWagered,
        BigDecimal totalWon
    ) {
        // Check if cashback already exists for this period
        if (cashbackRepository.existsByUserIdAndPeriodStartAndPeriodEnd(userId, periodStart, periodEnd)) {
            throw new CashbackException("Cashback already calculated for this period");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CashbackException("User not found"));

        // Calculate net loss (only if player lost money)
        BigDecimal netLoss = totalWagered.subtract(totalWon);

        if (netLoss.compareTo(BigDecimal.ZERO) <= 0) {
            // Player won or broke even, no cashback
            netLoss = BigDecimal.ZERO;
        }

        // Check minimum loss requirement
        if (netLoss.compareTo(MIN_LOSS_FOR_CASHBACK) < 0) {
            throw new CashbackException(
                String.format("Minimum loss of $%.2f required for cashback", MIN_LOSS_FOR_CASHBACK)
            );
        }

        // Get cashback percentage based on period
        BigDecimal cashbackPercentage = getCashbackPercentage(cashbackPeriod, user);

        // Calculate cashback amount
        BigDecimal cashbackAmount = netLoss
            .multiply(cashbackPercentage)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Create cashback record
        CashbackRecord cashback = CashbackRecord.builder()
            .userId(userId)
            .cashbackPeriod(cashbackPeriod)
            .periodStart(periodStart)
            .periodEnd(periodEnd)
            .totalWagered(totalWagered)
            .totalWon(totalWon)
            .netLoss(netLoss)
            .cashbackPercentage(cashbackPercentage)
            .cashbackAmount(cashbackAmount)
            .status(CashbackRecord.CashbackStatus.CLAIMABLE)
            .expiresAt(LocalDateTime.now().plusDays(CASHBACK_EXPIRY_DAYS))
            .build();

        cashback = cashbackRepository.save(cashback);

        log.info("Calculated {} cashback for user {}: loss={}, cashback={} ({}%)",
            cashbackPeriod, userId, netLoss, cashbackAmount, cashbackPercentage);

        return cashback;
    }

    /**
     * Calculate daily cashback
     */
    @Transactional
    public CashbackRecord calculateDailyCashback(
        String userId,
        LocalDate date,
        BigDecimal totalWagered,
        BigDecimal totalWon
    ) {
        return calculateCashback(
            userId,
            date,
            date,
            CashbackRecord.CashbackPeriod.DAILY,
            totalWagered,
            totalWon
        );
    }

    /**
     * Calculate weekly cashback
     */
    @Transactional
    public CashbackRecord calculateWeeklyCashback(
        String userId,
        LocalDate weekStart,
        BigDecimal totalWagered,
        BigDecimal totalWon
    ) {
        // Ensure weekStart is a Monday
        LocalDate monday = weekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);

        return calculateCashback(
            userId,
            monday,
            sunday,
            CashbackRecord.CashbackPeriod.WEEKLY,
            totalWagered,
            totalWon
        );
    }

    /**
     * Calculate monthly cashback
     */
    @Transactional
    public CashbackRecord calculateMonthlyCashback(
        String userId,
        LocalDate monthStart,
        BigDecimal totalWagered,
        BigDecimal totalWon
    ) {
        // Ensure monthStart is first day of month
        LocalDate firstDay = monthStart.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDay = monthStart.with(TemporalAdjusters.lastDayOfMonth());

        return calculateCashback(
            userId,
            firstDay,
            lastDay,
            CashbackRecord.CashbackPeriod.MONTHLY,
            totalWagered,
            totalWon
        );
    }

    /**
     * Claim cashback
     */
    @Transactional
    public CashbackRecord claimCashback(String cashbackId, String userId) {
        CashbackRecord cashback = cashbackRepository.findById(cashbackId)
            .orElseThrow(() -> new CashbackException("Cashback not found"));

        if (!cashback.getUserId().equals(userId)) {
            throw new CashbackException("Cashback does not belong to user");
        }

        if (!cashback.isClaimable()) {
            throw new CashbackException("Cashback is not claimable or has expired");
        }

        cashbackRepository.markAsClaimed(cashbackId, LocalDateTime.now());

        cashback.setStatus(CashbackRecord.CashbackStatus.CLAIMED);
        cashback.setClaimedAt(LocalDateTime.now());

        log.info("User {} claimed cashback {}: amount={}",
            userId, cashbackId, cashback.getCashbackAmount());

        return cashback;
    }

    /**
     * Get cashback percentage based on period and user VIP status
     */
    private BigDecimal getCashbackPercentage(CashbackRecord.CashbackPeriod period, User user) {
        BigDecimal basePercentage = switch (period) {
            case DAILY -> DAILY_CASHBACK_PERCENTAGE;
            case WEEKLY -> WEEKLY_CASHBACK_PERCENTAGE;
            case MONTHLY -> MONTHLY_CASHBACK_PERCENTAGE;
        };

        // TODO: Add VIP multiplier if user has VIP status
        // For now, return base percentage
        return basePercentage;
    }

    /**
     * Expire old cashbacks (scheduled task)
     */
    @Transactional
    public int expireOldCashbacks() {
        int expiredCount = cashbackRepository.expireOldCashbacks(LocalDateTime.now());

        if (expiredCount > 0) {
            log.info("Expired {} old cashbacks", expiredCount);
        }

        return expiredCount;
    }

    /**
     * Get total claimable cashback amount for a user
     */
    public BigDecimal getTotalClaimableCashback(String userId) {
        List<CashbackRecord> claimable = getClaimableCashbacks(userId);

        return claimable.stream()
            .map(CashbackRecord::getCashbackAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get cashback statistics for a user
     */
    public CashbackStats getUserCashbackStats(String userId) {
        List<CashbackRecord> allCashbacks = getUserCashbacks(userId);

        BigDecimal totalCashbackEarned = allCashbacks.stream()
            .filter(c -> c.getStatus() == CashbackRecord.CashbackStatus.CLAIMED)
            .map(CashbackRecord::getCashbackAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalClaimable = getTotalClaimableCashback(userId);

        long totalClaimed = allCashbacks.stream()
            .filter(c -> c.getStatus() == CashbackRecord.CashbackStatus.CLAIMED)
            .count();

        long totalExpired = allCashbacks.stream()
            .filter(c -> c.getStatus() == CashbackRecord.CashbackStatus.EXPIRED)
            .count();

        return CashbackStats.builder()
            .totalCashbackEarned(totalCashbackEarned)
            .totalClaimable(totalClaimable)
            .totalClaimed((int) totalClaimed)
            .totalExpired((int) totalExpired)
            .dailyPercentage(DAILY_CASHBACK_PERCENTAGE)
            .weeklyPercentage(WEEKLY_CASHBACK_PERCENTAGE)
            .monthlyPercentage(MONTHLY_CASHBACK_PERCENTAGE)
            .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class CashbackStats {
        private BigDecimal totalCashbackEarned;
        private BigDecimal totalClaimable;
        private Integer totalClaimed;
        private Integer totalExpired;
        private BigDecimal dailyPercentage;
        private BigDecimal weeklyPercentage;
        private BigDecimal monthlyPercentage;
    }
}
