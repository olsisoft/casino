package com.casino.user.service;

import com.casino.user.entity.AmlAlert;
import com.casino.user.entity.AmlTransaction;
import com.casino.user.exception.AmlException;
import com.casino.user.repository.AmlAlertRepository;
import com.casino.user.repository.AmlTransactionRepository;
import lombok.Builder;
import lombok.Data;
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
public class AmlService {

    private final AmlTransactionRepository amlTransactionRepository;
    private final AmlAlertRepository amlAlertRepository;

    // Thresholds
    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("10000.00");
    private static final BigDecimal ROUND_AMOUNT_THRESHOLD = new BigDecimal("1000.00");
    private static final int VELOCITY_24H_THRESHOLD = 10;
    private static final BigDecimal VOLUME_24H_THRESHOLD = new BigDecimal("50000.00");

    /**
     * Monitor transaction for AML compliance
     */
    @Transactional
    public AmlTransaction monitorTransaction(String userId, String transactionId,
                                             AmlTransaction.TransactionType type,
                                             BigDecimal amount, String currency,
                                             String ipAddress, String country) {
        // Create AML transaction record
        AmlTransaction amlTx = AmlTransaction.builder()
            .userId(userId)
            .transactionId(transactionId)
            .transactionType(type)
            .amount(amount)
            .currency(currency)
            .status(AmlTransaction.AmlStatus.PENDING_REVIEW)
            .ipAddress(ipAddress)
            .country(country)
            .build();

        // Calculate velocity metrics
        LocalDateTime last24h = LocalDateTime.now().minusHours(24);
        LocalDateTime last7d = LocalDateTime.now().minusDays(7);

        Long txCount24h = amlTransactionRepository.countByUserIdSince(userId, last24h);
        BigDecimal volume24h = amlTransactionRepository.sumAmountByUserIdSince(userId, last24h);
        if (volume24h == null) volume24h = BigDecimal.ZERO;

        Long txCount7d = amlTransactionRepository.countByUserIdSince(userId, last7d);
        BigDecimal volume7d = amlTransactionRepository.sumAmountByUserIdSince(userId, last7d);
        if (volume7d == null) volume7d = BigDecimal.ZERO;

        amlTx.setTransactionsLast24h(txCount24h.intValue());
        amlTx.setVolumeLast24h(volume24h);
        amlTx.setTransactionsLast7d(txCount7d.intValue());
        amlTx.setVolumeLast7d(volume7d);

        // Run risk checks
        runRiskChecks(amlTx);

        // Calculate risk score and level
        int riskScore = calculateRiskScore(amlTx);
        amlTx.setRiskScore(riskScore);
        amlTx.setRiskLevel(determineRiskLevel(riskScore));

        // Auto-approve low risk
        if (amlTx.getRiskLevel() == AmlTransaction.RiskLevel.LOW) {
            amlTx.setStatus(AmlTransaction.AmlStatus.CLEARED);
        } else if (amlTx.getRiskLevel() == AmlTransaction.RiskLevel.CRITICAL) {
            amlTx.setStatus(AmlTransaction.AmlStatus.BLOCKED);
            createAlert(amlTx, AmlAlert.AlertType.HIGH_VALUE_TRANSACTION, AmlAlert.AlertSeverity.CRITICAL);
        } else if (amlTx.getRiskLevel() == AmlTransaction.RiskLevel.HIGH) {
            amlTx.setStatus(AmlTransaction.AmlStatus.FLAGGED);
            createAlert(amlTx, AmlAlert.AlertType.SUSPICIOUS_PATTERN, AmlAlert.AlertSeverity.HIGH);
        }

        amlTransactionRepository.save(amlTx);

        log.info("AML transaction monitored: {} - Risk: {} - Status: {}",
            transactionId, amlTx.getRiskLevel(), amlTx.getStatus());

        return amlTx;
    }

    /**
     * Run various risk checks
     */
    private void runRiskChecks(AmlTransaction amlTx) {
        // High value check
        if (amlTx.getAmount().compareTo(HIGH_VALUE_THRESHOLD) >= 0) {
            amlTx.setIsHighRisk(true);
        }

        // Structuring check (multiple transactions just below reporting threshold)
        if (amlTx.getTransactionsLast24h() >= VELOCITY_24H_THRESHOLD) {
            amlTx.setIsStructured(true);
        }

        // Velocity check
        if (amlTx.getTransactionsLast24h() > VELOCITY_24H_THRESHOLD) {
            amlTx.setIsHighVelocity(true);
        }

        // Volume check
        if (amlTx.getVolumeLast24h().compareTo(VOLUME_24H_THRESHOLD) >= 0) {
            amlTx.setIsUnusualPattern(true);
        }

        // Round amount check (suspiciously round numbers)
        if (isRoundAmount(amlTx.getAmount())) {
            amlTx.setIsRoundAmount(true);
        }

        // High-risk country check (simplified)
        if (isHighRiskCountry(amlTx.getCountry())) {
            amlTx.setIsHighRisk(true);
        }
    }

    /**
     * Calculate risk score
     */
    private int calculateRiskScore(AmlTransaction amlTx) {
        int score = 0;

        // Amount-based risk
        if (amlTx.getAmount().compareTo(new BigDecimal("50000")) >= 0) score += 40;
        else if (amlTx.getAmount().compareTo(HIGH_VALUE_THRESHOLD) >= 0) score += 25;
        else if (amlTx.getAmount().compareTo(new BigDecimal("5000")) >= 0) score += 15;

        // Pattern-based risk
        if (Boolean.TRUE.equals(amlTx.getIsStructured())) score += 30;
        if (Boolean.TRUE.equals(amlTx.getIsHighVelocity())) score += 20;
        if (Boolean.TRUE.equals(amlTx.getIsUnusualPattern())) score += 15;
        if (Boolean.TRUE.equals(amlTx.getIsRoundAmount())) score += 10;

        // Geographic risk
        if (Boolean.TRUE.equals(amlTx.getIsHighRisk())) score += 25;

        // Compliance flags
        if (Boolean.TRUE.equals(amlTx.getIsPep())) score += 30;
        if (Boolean.TRUE.equals(amlTx.getIsSanctioned())) score += 50;

        return Math.min(score, 100);
    }

    /**
     * Determine risk level from score
     */
    private AmlTransaction.RiskLevel determineRiskLevel(int score) {
        if (score >= 75) return AmlTransaction.RiskLevel.CRITICAL;
        if (score >= 50) return AmlTransaction.RiskLevel.HIGH;
        if (score >= 25) return AmlTransaction.RiskLevel.MEDIUM;
        return AmlTransaction.RiskLevel.LOW;
    }

    /**
     * Check if amount is suspiciously round
     */
    private boolean isRoundAmount(BigDecimal amount) {
        if (amount.compareTo(ROUND_AMOUNT_THRESHOLD) < 0) return false;

        // Check if divisible by 1000, 5000, 10000
        BigDecimal[] roundNumbers = {
            new BigDecimal("10000"),
            new BigDecimal("5000"),
            new BigDecimal("1000")
        };

        for (BigDecimal roundNumber : roundNumbers) {
            if (amount.remainder(roundNumber).compareTo(BigDecimal.ZERO) == 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if country is high risk (simplified)
     */
    private boolean isHighRiskCountry(String country) {
        // In production, use FATF list and sanctions lists
        return country != null && country.equalsIgnoreCase("XX");
    }

    /**
     * Create AML alert
     */
    @Transactional
    public AmlAlert createAlert(AmlTransaction amlTx, AmlAlert.AlertType type, AmlAlert.AlertSeverity severity) {
        AmlAlert alert = AmlAlert.builder()
            .userId(amlTx.getUserId())
            .transactionId(amlTx.getTransactionId())
            .amlTransactionId(amlTx.getId())
            .alertType(type)
            .severity(severity)
            .status(AmlAlert.AlertStatus.NEW)
            .title(generateAlertTitle(type))
            .description(generateAlertDescription(amlTx, type))
            .triggeredRule(type.name())
            .build();

        return amlAlertRepository.save(alert);
    }

    /**
     * Generate alert title
     */
    private String generateAlertTitle(AmlAlert.AlertType type) {
        return switch (type) {
            case HIGH_VALUE_TRANSACTION -> "High Value Transaction Detected";
            case SUSPICIOUS_PATTERN -> "Suspicious Pattern Detected";
            case RAPID_MOVEMENT -> "Rapid Fund Movement";
            case STRUCTURING -> "Possible Structuring Activity";
            case VELOCITY_THRESHOLD -> "Velocity Threshold Exceeded";
            default -> "AML Alert: " + type.name();
        };
    }

    /**
     * Generate alert description
     */
    private String generateAlertDescription(AmlTransaction amlTx, AmlAlert.AlertType type) {
        return String.format("User %s triggered %s alert. Amount: %s, Risk Score: %d, Transactions 24h: %d",
            amlTx.getUserId(),
            type.name(),
            amlTx.getAmount(),
            amlTx.getRiskScore(),
            amlTx.getTransactionsLast24h()
        );
    }

    /**
     * Review AML transaction (admin)
     */
    @Transactional
    public AmlTransaction reviewTransaction(String amlTransactionId,
                                            AmlTransaction.ReviewDecision decision,
                                            String reviewNotes,
                                            String reviewedBy) {
        AmlTransaction amlTx = amlTransactionRepository.findById(amlTransactionId)
            .orElseThrow(() -> new AmlException("AML transaction not found"));

        amlTx.setReviewedBy(reviewedBy);
        amlTx.setReviewedAt(LocalDateTime.now());
        amlTx.setReviewNotes(reviewNotes);
        amlTx.setReviewDecision(decision);

        switch (decision) {
            case APPROVED -> amlTx.setStatus(AmlTransaction.AmlStatus.CLEARED);
            case REJECTED -> amlTx.setStatus(AmlTransaction.AmlStatus.BLOCKED);
            case REQUIRES_INVESTIGATION, ESCALATED -> amlTx.setStatus(AmlTransaction.AmlStatus.FLAGGED);
        }

        return amlTransactionRepository.save(amlTx);
    }

    /**
     * Resolve AML alert (admin)
     */
    @Transactional
    public AmlAlert resolveAlert(String alertId,
                                 AmlAlert.AlertResolution resolution,
                                 String resolutionNotes,
                                 String resolvedBy) {
        AmlAlert alert = amlAlertRepository.findById(alertId)
            .orElseThrow(() -> new AmlException("Alert not found"));

        alert.setStatus(AmlAlert.AlertStatus.RESOLVED);
        alert.setResolution(resolution);
        alert.setResolutionNotes(resolutionNotes);
        alert.setResolvedBy(resolvedBy);
        alert.setResolvedAt(LocalDateTime.now());

        return amlAlertRepository.save(alert);
    }

    /**
     * File SAR (Suspicious Activity Report)
     */
    @Transactional
    public AmlTransaction fileSar(String amlTransactionId, String filedBy) {
        AmlTransaction amlTx = amlTransactionRepository.findById(amlTransactionId)
            .orElseThrow(() -> new AmlException("AML transaction not found"));

        String sarId = "SAR-" + System.currentTimeMillis();
        amlTx.setSarFiled(true);
        amlTx.setSarId(sarId);
        amlTx.setSarFiledAt(LocalDateTime.now());
        amlTx.setStatus(AmlTransaction.AmlStatus.REPORTED);

        log.warn("SAR filed: {} for transaction: {} by: {}", sarId, amlTransactionId, filedBy);

        return amlTransactionRepository.save(amlTx);
    }

    /**
     * Get user's AML transactions
     */
    public List<AmlTransaction> getUserTransactions(String userId) {
        return amlTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get flagged transactions (admin)
     */
    public List<AmlTransaction> getFlaggedTransactions() {
        return amlTransactionRepository.findByStatus(AmlTransaction.AmlStatus.FLAGGED);
    }

    /**
     * Get alerts by status (admin)
     */
    public List<AmlAlert> getAlertsByStatus(AmlAlert.AlertStatus status) {
        return amlAlertRepository.findByStatus(status);
    }

    /**
     * Get user's alerts
     */
    public List<AmlAlert> getUserAlerts(String userId) {
        return amlAlertRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get AML statistics
     */
    public AmlStatistics getStatistics() {
        Long newAlerts = amlAlertRepository.countByStatus(AmlAlert.AlertStatus.NEW);
        Long criticalAlerts = amlAlertRepository.countBySeverity(AmlAlert.AlertSeverity.CRITICAL);
        Long flaggedTx = amlTransactionRepository.countByStatusAndRiskLevel(
            AmlTransaction.AmlStatus.FLAGGED,
            AmlTransaction.RiskLevel.HIGH
        );

        return AmlStatistics.builder()
            .newAlerts(newAlerts)
            .criticalAlerts(criticalAlerts)
            .flaggedTransactions(flaggedTx)
            .build();
    }

    @Data
    @Builder
    public static class AmlStatistics {
        private Long newAlerts;
        private Long criticalAlerts;
        private Long flaggedTransactions;
    }
}
