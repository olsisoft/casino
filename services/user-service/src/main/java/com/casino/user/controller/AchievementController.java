package com.casino.user.controller;

import com.casino.user.entity.Achievement;
import com.casino.user.entity.UserAchievement;
import com.casino.user.service.AchievementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping
    public ResponseEntity<List<Achievement>> getAllAchievements() {
        log.info("GET /achievements");

        List<Achievement> achievements = achievementService.getVisibleAchievements();

        return ResponseEntity.ok(achievements);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Achievement>> getAllAchievementsIncludingHidden() {
        log.info("GET /achievements/all");

        List<Achievement> achievements = achievementService.getAllAchievements();

        return ResponseEntity.ok(achievements);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Achievement>> getAchievementsByCategory(
        @PathVariable Achievement.AchievementCategory category
    ) {
        log.info("GET /achievements/category/{}", category);

        List<Achievement> achievements = achievementService.getAchievementsByCategory(category);

        return ResponseEntity.ok(achievements);
    }

    @GetMapping("/my")
    public ResponseEntity<List<UserAchievement>> getMyAchievements(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /achievements/my - userId: {}", userId);

        List<UserAchievement> userAchievements = achievementService.getUserAchievements(userId);

        return ResponseEntity.ok(userAchievements);
    }

    @GetMapping("/my/unlocked")
    public ResponseEntity<List<UserAchievement>> getMyUnlockedAchievements(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /achievements/my/unlocked - userId: {}", userId);

        List<UserAchievement> unlocked = achievementService.getUnlockedAchievements(userId);

        return ResponseEntity.ok(unlocked);
    }

    @GetMapping("/my/in-progress")
    public ResponseEntity<List<UserAchievement>> getMyInProgressAchievements(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /achievements/my/in-progress - userId: {}", userId);

        List<UserAchievement> inProgress = achievementService.getInProgressAchievements(userId);

        return ResponseEntity.ok(inProgress);
    }

    @GetMapping("/my/unclaimed")
    public ResponseEntity<List<UserAchievement>> getMyUnclaimedRewards(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /achievements/my/unclaimed - userId: {}", userId);

        List<UserAchievement> unclaimed = achievementService.getUnclaimedRewards(userId);

        return ResponseEntity.ok(unclaimed);
    }

    @GetMapping("/my/stats")
    public ResponseEntity<AchievementService.AchievementStats> getMyStats(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /achievements/my/stats - userId: {}", userId);

        AchievementService.AchievementStats stats = achievementService.getUserStats(userId);

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/my/initialize")
    public ResponseEntity<Void> initializeMyAchievements(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("POST /achievements/my/initialize - userId: {}", userId);

        achievementService.initializeUserAchievements(userId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{achievementId}/claim")
    public ResponseEntity<AchievementService.ClaimRewardResponse> claimReward(
        @PathVariable String achievementId,
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("POST /achievements/{}/claim - userId: {}", achievementId, userId);

        AchievementService.ClaimRewardResponse response = achievementService.claimReward(userId, achievementId);

        return ResponseEntity.ok(response);
    }

    // Admin endpoints

    @PostMapping("/admin")
    public ResponseEntity<Achievement> createAchievement(
        @Valid @RequestBody Achievement achievement
    ) {
        log.info("POST /achievements/admin - code: {}", achievement.getCode());

        Achievement created = achievementService.createAchievement(achievement);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/admin/{achievementId}")
    public ResponseEntity<Achievement> updateAchievement(
        @PathVariable String achievementId,
        @Valid @RequestBody Achievement achievement
    ) {
        log.info("PUT /achievements/admin/{}", achievementId);

        Achievement updated = achievementService.updateAchievement(achievementId, achievement);

        return ResponseEntity.ok(updated);
    }
}
