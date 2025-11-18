package com.casino.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "country", length = 2)
    private String country;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "level", nullable = false)
    private Integer level;

    @Column(name = "xp", nullable = false)
    private Long xp;

    @Column(name = "total_wagered", nullable = false)
    private Long totalWagered;

    @Column(name = "total_won", nullable = false)
    private Long totalWon;

    @Column(name = "games_played", nullable = false)
    private Integer gamesPlayed;

    @Column(name = "achievements_unlocked", nullable = false)
    private Integer achievementsUnlocked;

    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak;

    @Column(name = "best_streak", nullable = false)
    private Integer bestStreak;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (level == null) level = 1;
        if (xp == null) xp = 0L;
        if (totalWagered == null) totalWagered = 0L;
        if (totalWon == null) totalWon = 0L;
        if (gamesPlayed == null) gamesPlayed = 0;
        if (achievementsUnlocked == null) achievementsUnlocked = 0;
        if (currentStreak == null) currentStreak = 0;
        if (bestStreak == null) bestStreak = 0;
    }
}
