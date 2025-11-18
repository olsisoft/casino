package com.casino.user.controller;

import com.casino.user.entity.LeaderboardEntry;
import com.casino.user.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/leaderboards")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/daily")
    public ResponseEntity<List<LeaderboardEntry>> getDailyLeaderboard(
        @RequestParam(defaultValue = "100") int limit
    ) {
        log.info("GET /leaderboards/daily?limit={}", limit);

        List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(
            LeaderboardEntry.PeriodType.DAILY,
            limit
        );

        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/weekly")
    public ResponseEntity<List<LeaderboardEntry>> getWeeklyLeaderboard(
        @RequestParam(defaultValue = "100") int limit
    ) {
        log.info("GET /leaderboards/weekly?limit={}", limit);

        List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(
            LeaderboardEntry.PeriodType.WEEKLY,
            limit
        );

        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<LeaderboardEntry>> getMonthlyLeaderboard(
        @RequestParam(defaultValue = "100") int limit
    ) {
        log.info("GET /leaderboards/monthly?limit={}", limit);

        List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(
            LeaderboardEntry.PeriodType.MONTHLY,
            limit
        );

        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/all-time")
    public ResponseEntity<List<LeaderboardEntry>> getAllTimeLeaderboard(
        @RequestParam(defaultValue = "100") int limit
    ) {
        log.info("GET /leaderboards/all-time?limit={}", limit);

        List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(
            LeaderboardEntry.PeriodType.ALL_TIME,
            limit
        );

        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/my-rank/daily")
    public ResponseEntity<LeaderboardEntry> getMyDailyRank(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /leaderboards/my-rank/daily - userId: {}", userId);

        Optional<LeaderboardEntry> entry = leaderboardService.getUserRank(
            userId,
            LeaderboardEntry.PeriodType.DAILY
        );

        return entry.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my-rank/weekly")
    public ResponseEntity<LeaderboardEntry> getMyWeeklyRank(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /leaderboards/my-rank/weekly - userId: {}", userId);

        Optional<LeaderboardEntry> entry = leaderboardService.getUserRank(
            userId,
            LeaderboardEntry.PeriodType.WEEKLY
        );

        return entry.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my-rank/monthly")
    public ResponseEntity<LeaderboardEntry> getMyMonthlyRank(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /leaderboards/my-rank/monthly - userId: {}", userId);

        Optional<LeaderboardEntry> entry = leaderboardService.getUserRank(
            userId,
            LeaderboardEntry.PeriodType.MONTHLY
        );

        return entry.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my-rank/all-time")
    public ResponseEntity<LeaderboardEntry> getMyAllTimeRank(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /leaderboards/my-rank/all-time - userId: {}", userId);

        Optional<LeaderboardEntry> entry = leaderboardService.getUserRank(
            userId,
            LeaderboardEntry.PeriodType.ALL_TIME
        );

        return entry.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my-stats")
    public ResponseEntity<LeaderboardService.LeaderboardStats> getMyStats(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /leaderboards/my-stats - userId: {}", userId);

        LeaderboardService.LeaderboardStats stats = leaderboardService.getUserLeaderboardStats(userId);

        return ResponseEntity.ok(stats);
    }
}
