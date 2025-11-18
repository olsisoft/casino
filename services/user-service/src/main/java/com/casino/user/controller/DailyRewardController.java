package com.casino.user.controller;

import com.casino.user.entity.DailyReward;
import com.casino.user.service.DailyRewardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/daily-rewards")
@RequiredArgsConstructor
public class DailyRewardController {

    private final DailyRewardService dailyRewardService;

    @GetMapping("/today")
    public ResponseEntity<DailyReward> getTodayReward(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /daily-rewards/today - userId: {}", userId);

        return dailyRewardService.getTodayReward(userId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/history")
    public ResponseEntity<List<DailyReward>> getRewardHistory(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /daily-rewards/history - userId: {}", userId);
        List<DailyReward> history = dailyRewardService.getRewardHistory(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/streak")
    public ResponseEntity<StreakResponse> getCurrentStreak(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /daily-rewards/streak - userId: {}", userId);
        int streak = dailyRewardService.getCurrentStreak(userId);
        boolean canClaim = dailyRewardService.canClaimToday(userId);

        return ResponseEntity.ok(new StreakResponse(streak, canClaim));
    }

    @PostMapping("/claim")
    public ResponseEntity<DailyReward> claimDailyReward(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("POST /daily-rewards/claim - userId: {}", userId);

        DailyReward reward = dailyRewardService.claimDailyReward(userId);

        return ResponseEntity.ok(reward);
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<DailyRewardService.StreakRewardInfo>> getStreakCalendar() {
        log.info("GET /daily-rewards/calendar");
        List<DailyRewardService.StreakRewardInfo> calendar = dailyRewardService.getStreakRewardCalendar();
        return ResponseEntity.ok(calendar);
    }

    @GetMapping("/stats")
    public ResponseEntity<DailyRewardService.DailyRewardStats> getUserStats(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /daily-rewards/stats - userId: {}", userId);
        DailyRewardService.DailyRewardStats stats = dailyRewardService.getUserStats(userId);
        return ResponseEntity.ok(stats);
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class StreakResponse {
        private Integer currentStreak;
        private Boolean canClaimToday;
    }
}
