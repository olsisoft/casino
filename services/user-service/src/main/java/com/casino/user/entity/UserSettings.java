package com.casino.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings {

    @Id
    @Column(name = "user_id")
    private String userId;

    // Notification preferences
    @Column(name = "email_notifications", nullable = false)
    private Boolean emailNotifications;

    @Column(name = "push_notifications", nullable = false)
    private Boolean pushNotifications;

    @Column(name = "in_app_notifications", nullable = false)
    private Boolean inAppNotifications;

    // Privacy
    @Column(name = "show_profile", nullable = false)
    private Boolean showProfile;

    @Column(name = "show_stats", nullable = false)
    private Boolean showStats;

    // Responsible gaming
    @Column(name = "daily_deposit_limit", precision = 19, scale = 2)
    private BigDecimal dailyDepositLimit;

    @Column(name = "weekly_deposit_limit", precision = 19, scale = 2)
    private BigDecimal weeklyDepositLimit;

    @Column(name = "monthly_deposit_limit", precision = 19, scale = 2)
    private BigDecimal monthlyDepositLimit;

    @Column(name = "session_time_limit")
    private Integer sessionTimeLimit; // in minutes

    @Column(name = "cool_off_until")
    private LocalDateTime coolOffUntil;

    // Preferences
    @Column(name = "language", length = 5, nullable = false)
    private String language;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "theme", length = 10, nullable = false)
    private String theme;

    @Column(name = "sound_enabled", nullable = false)
    private Boolean soundEnabled;

    @Column(name = "music_enabled", nullable = false)
    private Boolean musicEnabled;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (emailNotifications == null) emailNotifications = true;
        if (pushNotifications == null) pushNotifications = true;
        if (inAppNotifications == null) inAppNotifications = true;
        if (showProfile == null) showProfile = true;
        if (showStats == null) showStats = true;
        if (language == null) language = "en";
        if (currency == null) currency = "USD";
        if (theme == null) theme = "dark";
        if (soundEnabled == null) soundEnabled = true;
        if (musicEnabled == null) musicEnabled = true;
    }
}
