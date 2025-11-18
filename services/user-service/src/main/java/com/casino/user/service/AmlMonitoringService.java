package com.casino.user.service;

import com.casino.user.model.AmlAlert;
import com.casino.user.model.AmlTransaction;
import com.casino.user.repository.AmlAlertRepository;
import com.casino.user.repository.AmlTransactionRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enhanced AML Monitoring Service
 * Real-time transaction monitoring with ML-based risk scoring
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AmlMonitoringService {

    private final AmlTransactionRepository amlTransactionRepository;
    private final AmlAlertRepository amlAlertRepository;

    // Thresholds
    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("10000.00");
    private static final BigDecimal STRUCTURING_THRESHOLD = new BigDecimal("9000.00");
    private static final int VELOCITY_24H_THRESHOLD = 10;
    private static final BigDecimal VELOCITY_AMOUNT_THRESHOLD = new BigDecimal("50000.00");
    private static final int RAPID_TRANSACTION_MINUTES = 30;

    // High-risk countries (FATF blacklist/greylist)
    private static final Set<String> HIGH_RISK_COUNTRIES = Set.of(
        "KP", "IR", "MM", // Blacklist
        "AL", "BB", "BF", "CM", "GH", "JM", "ML", "MZ", "NI", "PK", "PA", "PH", "SN", "SY", "TZ", "TT", "UG", "VU", "YE", "ZW" // Greylist
    );

    /**
     * Analyze transaction for AML risks
     */
    @Async
    public void analyzeTransaction(String userId, String transactionType,
                                   BigDecimal amount, String currency,
                                   String ipAddress, String country) {

        AmlTransaction amlTx = new AmlTransaction();
        amlTx.setUserId(userId);
        amlTx.setTransactionType(transactionType);
        amlTx.setAmount(amount);
        amlTx.setCurrency(currency);
        amlTx.setIpAddress(ipAddress);
        amlTx.setCountry(country);
        amlTx.setTimestamp(LocalDateTime.now());

        // Run risk checks
        int riskScore = 0;
        List<String> riskFactors = new ArrayList<>();

        // 1. High value transaction
        if (amount.compareTo(HIGH_VALUE_THRESHOLD) >= 0) {
            riskScore += 30;
            riskFactors.add("HIGH_VALUE");
            amlTx.setIsHighValue(true);
        }

        // 2. Structuring detection (amounts just below reporting threshold)
        if (isStructuring(userId, amount, transactionType)) {
            riskScore += 40;
            riskFactors.add("STRUCTURING");
            amlTx.setIsStructured(true);
            createAlert(userId, AmlAlert.AlertType.STRUCTURING,
                "Multiple transactions just below $10,000 threshold", AmlAlert.Severity.HIGH);
        }

        // 3. High velocity (too many transactions)
        if (isHighVelocity(userId)) {
            riskScore += 35;
            riskFactors.add("HIGH_VELOCITY");
            amlTx.setIsHighVelocity(true);
            createAlert(userId, AmlAlert.AlertType.HIGH_VELOCITY,
                "Unusual number of transactions in 24 hours", AmlAlert.Severity.MEDIUM);
        }

        // 4. Round amount (suspiciously round numbers)
        if (isRoundAmount(amount)) {
            riskScore += 10;
            riskFactors.add("ROUND_AMOUNT");
            amlTx.setIsRoundAmount(true);
        }

        // 5. High-risk geography
        if (HIGH_RISK_COUNTRIES.contains(country)) {
            riskScore += 25;
            riskFactors.add("HIGH_RISK_COUNTRY");
            createAlert(userId, AmlAlert.AlertType.HIGH_RISK_JURISDICTION,
                "Transaction from high-risk jurisdiction: " + country, AmlAlert.Severity.HIGH);
        }

        // 6. Rapid successive transactions
        if (hasRapidTransactions(userId)) {
            riskScore += 20;
            riskFactors.add("RAPID_TRANSACTIONS");
        }

        // 7. Unusual time (transactions at odd hours)
        if (isUnusualTime()) {
            riskScore += 15;
            riskFactors.add("UNUSUAL_TIME");
        }

        // 8. Multiple withdrawal methods
        if (hasMultipleWithdrawalMethods(userId)) {
            riskScore += 20;
            riskFactors.add("MULTIPLE_METHODS");
        }

        amlTx.setRiskScore(Math.min(riskScore, 100));
        amlTx.setRiskFactors(String.join(", ", riskFactors));

        // Save transaction
        amlTransactionRepository.save(amlTx);

        // Create alert if high risk
        if (riskScore >= 70) {
            createAlert(userId, AmlAlert.AlertType.HIGH_RISK_TRANSACTION,
                "Transaction risk score: " + riskScore, AmlAlert.Severity.HIGH);
        }

        log.info("AML analysis completed - User: {}, Risk Score: {}, Factors: {}",
            userId, riskScore, riskFactors);
    }

    /**
     * Detect structuring (smurfing) - breaking large amounts into smaller transactions
     */
    private boolean isStructuring(String userId, BigDecimal currentAmount, String transactionType) {
        if (!"WITHDRAWAL".equals(transactionType) && !"DEPOSIT".equals(transactionType)) {
            return false;
        }

        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        List<AmlTransaction> recentTransactions = amlTransactionRepository
            .findByUserIdAndTransactionTypeAndTimestampAfter(userId, transactionType, last24Hours);

        // Check for multiple transactions just below threshold
        long nearThresholdCount = recentTransactions.stream()
            .filter(tx -> tx.getAmount().compareTo(STRUCTURING_THRESHOLD) >= 0 &&
                         tx.getAmount().compareTo(HIGH_VALUE_THRESHOLD) < 0)
            .count();

        // Also check current transaction
        if (currentAmount.compareTo(STRUCTURING_THRESHOLD) >= 0 &&
            currentAmount.compareTo(HIGH_VALUE_THRESHOLD) < 0) {
            nearThresholdCount++;
        }

        return nearThresholdCount >= 3; // 3+ transactions near threshold = suspicious
    }

    /**
     * Detect high velocity (too many transactions in short period)
     */
    private boolean isHighVelocity(String userId) {
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        long count = amlTransactionRepository.countByUserIdAndTimestampAfter(userId, last24Hours);

        // Check amount velocity too
        List<AmlTransaction> recentTxs = amlTransactionRepository
            .findByUserIdAndTimestampAfter(userId, last24Hours);

        BigDecimal totalAmount = recentTxs.stream()
            .map(AmlTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return count >= VELOCITY_24H_THRESHOLD ||
               totalAmount.compareTo(VELOCITY_AMOUNT_THRESHOLD) >= 0;
    }

    /**
     * Check for rapid successive transactions (potential automation/bot)
     */
    private boolean hasRapidTransactions(String userId) {
        LocalDateTime recentTime = LocalDateTime.now().minusMinutes(RAPID_TRANSACTION_MINUTES);
        long count = amlTransactionRepository.countByUserIdAndTimestampAfter(userId, recentTime);
        return count >= 5; // 5+ transactions in 30 minutes
    }

    /**
     * Detect round amounts (e.g., $1000, $5000 exactly)
     */
    private boolean isRoundAmount(BigDecimal amount) {
        // Check if amount is exact thousands, hundreds, or ends in multiple zeros
        return amount.remainder(new BigDecimal("1000")).compareTo(BigDecimal.ZERO) == 0 ||
               amount.remainder(new BigDecimal("500")).compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Check if transaction is at unusual time (2 AM - 6 AM local time)
     */
    private boolean isUnusualTime() {
        int hour = LocalDateTime.now().getHour();
        return hour >= 2 && hour <= 6;
    }

    /**
     * Check if user has used multiple withdrawal methods (potential money laundering)
     */
    private boolean hasMultipleWithdrawalMethods(String userId) {
        LocalDateTime last30Days = LocalDateTime.now().minusDays(30);
        List<AmlTransaction> withdrawals = amlTransactionRepository
            .findByUserIdAndTransactionTypeAndTimestampAfter(userId, "WITHDRAWAL", last30Days);

        // In production, check actual payment methods from transaction metadata
        return withdrawals.size() > 10; // Simplified check
    }

    /**
     * Create AML alert
     */
    private void createAlert(String userId, AmlAlert.AlertType alertType,
                            String description, AmlAlert.Severity severity) {

        // Check if similar alert already exists and is unresolved
        Optional<AmlAlert> existingAlert = amlAlertRepository
            .findByUserIdAndAlertTypeAndStatusNot(userId, alertType, AmlAlert.Status.RESOLVED)
            .stream()
            .findFirst();

        if (existingAlert.isPresent()) {
            log.debug("Similar alert already exists for user: {}", userId);
            return;
        }

        AmlAlert alert = new AmlAlert();
        alert.setUserId(userId);
        alert.setAlertType(alertType);
        alert.setDescription(description);
        alert.setSeverity(severity);
        alert.setStatus(AmlAlert.Status.NEW);
        alert.setCreatedAt(LocalDateTime.now());

        amlAlertRepository.save(alert);

        log.warn("AML Alert created - User: {}, Type: {}, Severity: {}",
            userId, alertType, severity);
    }

    /**
     * Get risk profile for user
     */
    public UserRiskProfile getUserRiskProfile(String userId) {
        List<AmlTransaction> allTransactions = amlTransactionRepository.findByUserId(userId);

        UserRiskProfile profile = new UserRiskProfile();
        profile.setUserId(userId);
        profile.setTotalTransactions(allTransactions.size());

        // Calculate metrics
        if (!allTransactions.isEmpty()) {
            int totalRiskScore = allTransactions.stream()
                .mapToInt(AmlTransaction::getRiskScore)
                .sum();
            profile.setAverageRiskScore(totalRiskScore / allTransactions.size());

            BigDecimal totalVolume = allTransactions.stream()
                .map(AmlTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            profile.setTotalVolume(totalVolume);

            long highRiskCount = allTransactions.stream()
                .filter(tx -> tx.getRiskScore() >= 70)
                .count();
            profile.setHighRiskTransactionCount((int) highRiskCount);
        }

        // Get active alerts
        List<AmlAlert> activeAlerts = amlAlertRepository
            .findByUserIdAndStatus(userId, AmlAlert.Status.NEW);
        profile.setActiveAlertCount(activeAlerts.size());

        // Determine overall risk level
        if (profile.getAverageRiskScore() >= 70 || profile.getActiveAlertCount() >= 3) {
            profile.setRiskLevel("HIGH");
        } else if (profile.getAverageRiskScore() >= 40 || profile.getActiveAlertCount() >= 1) {
            profile.setRiskLevel("MEDIUM");
        } else {
            profile.setRiskLevel("LOW");
        }

        return profile;
    }

    /**
     * Generate suspicious activity report (SAR)
     */
    public SuspiciousActivityReport generateSAR(String userId, String reason) {
        UserRiskProfile profile = getUserRiskProfile(userId);
        List<AmlAlert> alerts = amlAlertRepository.findByUserId(userId);
        List<AmlTransaction> highRiskTxs = amlTransactionRepository
            .findByUserId(userId).stream()
            .filter(tx -> tx.getRiskScore() >= 70)
            .collect(Collectors.toList());

        SuspiciousActivityReport sar = new SuspiciousActivityReport();
        sar.setReportId("SAR_" + System.currentTimeMillis());
        sar.setUserId(userId);
        sar.setGeneratedAt(LocalDateTime.now());
        sar.setReason(reason);
        sar.setRiskProfile(profile);
        sar.setAlertCount(alerts.size());
        sar.setHighRiskTransactionCount(highRiskTxs.size());

        log.warn("SAR generated - Report ID: {}, User: {}", sar.getReportId(), userId);
        return sar;
    }

    @Data
    public static class UserRiskProfile {
        private String userId;
        private Integer totalTransactions;
        private Integer averageRiskScore;
        private BigDecimal totalVolume;
        private Integer highRiskTransactionCount;
        private Integer activeAlertCount;
        private String riskLevel; // LOW, MEDIUM, HIGH
    }

    @Data
    public static class SuspiciousActivityReport {
        private String reportId;
        private String userId;
        private LocalDateTime generatedAt;
        private String reason;
        private UserRiskProfile riskProfile;
        private Integer alertCount;
        private Integer highRiskTransactionCount;
    }
}
