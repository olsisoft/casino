package com.casino.user.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {
    private String userId;
    private String firstName;
    private String lastName;
    private String avatarUrl;
    private Integer level;
    private Long xp;
    private Long totalWagered;
    private Long totalWon;
    private Integer gamesPlayed;
    private Integer achievementsUnlocked;
    private Integer currentStreak;
    private Integer bestStreak;
}
