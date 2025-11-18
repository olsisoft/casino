package com.casino.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "aml_alerts", indexes = {
    @Index(name = "idx_alert_user", columnList = "userId"),
    @Index(name = "idx_alert_severity", columnList = "severity"),
    @Index(name = "idx_alert_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmlAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    // Related entities
    private String transactionId;
    private String amlTransactionId;

    // Rule that triggered the alert
    private String triggeredRule;

    // Assignment
    private String assignedTo; // Compliance officer user ID
    private LocalDateTime assignedAt;

    // Resolution
    private String resolvedBy;
    private LocalDateTime resolvedAt;

    @Column(length = 2000)
    private String resolutionNotes;

    @Enumerated(EnumType.STRING)
    private AlertResolution resolution;

    // Escalation
    private Boolean isEscalated = false;
    private LocalDateTime escalatedAt;
    private String escalatedTo;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum AlertType {
        SUSPICIOUS_PATTERN,
        HIGH_VALUE_TRANSACTION,
        RAPID_MOVEMENT,
        STRUCTURING,
        UNUSUAL_ACTIVITY,
        PEP_TRANSACTION,
        SANCTIONED_COUNTRY,
        VELOCITY_THRESHOLD,
        ROUND_AMOUNT_PATTERN,
        DORMANT_ACCOUNT_ACTIVITY,
        MULTIPLE_ACCOUNTS,
        THIRD_PARTY_DEPOSIT
    }

    public enum AlertSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum AlertStatus {
        NEW,
        ASSIGNED,
        INVESTIGATING,
        PENDING_REVIEW,
        RESOLVED,
        ESCALATED,
        CLOSED
    }

    public enum AlertResolution {
        FALSE_POSITIVE,
        LEGITIMATE,
        SUSPICIOUS_MONITORED,
        SAR_FILED,
        ACCOUNT_FROZEN,
        ACCOUNT_CLOSED
    }
}
