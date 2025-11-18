package com.casino.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_settings", indexes = {
    @Index(name = "idx_setting_key", columnList = "settingKey"),
    @Index(name = "idx_setting_category", columnList = "category")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String settingKey;

    @Column(nullable = false)
    private String settingValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ValueType valueType;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Boolean isPublic = false; // Can be accessed by clients

    @Column(nullable = false)
    private Boolean isEditable = true;

    private String defaultValue;

    private String updatedBy; // Admin user ID

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

    public enum Category {
        GENERAL,
        SECURITY,
        GAMES,
        PAYMENTS,
        BONUSES,
        LIMITS,
        COMPLIANCE,
        NOTIFICATIONS,
        MAINTENANCE
    }

    public enum ValueType {
        STRING,
        INTEGER,
        DECIMAL,
        BOOLEAN,
        JSON
    }
}
