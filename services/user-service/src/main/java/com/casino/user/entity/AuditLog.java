package com.casino.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_admin", columnList = "adminUserId"),
    @Index(name = "idx_audit_entity", columnList = "entityType,entityId"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_created", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String adminUserId;

    private String adminUsername;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType action;

    @Column(nullable = false)
    private String entityType; // User, Game, Transaction, etc.

    private String entityId;

    @Column(length = 2000)
    private String description;

    @Column(length = 5000)
    private String oldValue; // JSON of old state

    @Column(length = 5000)
    private String newValue; // JSON of new state

    private String ipAddress;
    private String userAgent;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum ActionType {
        CREATE,
        UPDATE,
        DELETE,
        APPROVE,
        REJECT,
        SUSPEND,
        ACTIVATE,
        DEACTIVATE,
        LOGIN,
        LOGOUT,
        PAYOUT,
        REFUND,
        ADJUST_BALANCE,
        CHANGE_STATUS,
        VIEW_SENSITIVE_DATA,
        EXPORT_DATA,
        SYSTEM_CONFIG_CHANGE
    }

    public enum Severity {
        INFO,
        WARNING,
        CRITICAL
    }
}
