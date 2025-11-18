package com.casino.user.controller;

import com.casino.user.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/overview")
    public ResponseEntity<AdminDashboardService.DashboardOverview> getOverview() {
        log.info("GET /admin/dashboard/overview");

        AdminDashboardService.DashboardOverview overview = adminDashboardService.getOverview();

        return ResponseEntity.ok(overview);
    }

    @GetMapping("/users/statistics")
    public ResponseEntity<AdminDashboardService.UserStatistics> getUserStatistics(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("GET /admin/dashboard/users/statistics?startDate={}&endDate={}", startDate, endDate);

        AdminDashboardService.UserStatistics stats = adminDashboardService.getUserStatistics(startDate, endDate);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/financial/overview")
    public ResponseEntity<AdminDashboardService.FinancialOverview> getFinancialOverview(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("GET /admin/dashboard/financial/overview?startDate={}&endDate={}", startDate, endDate);

        AdminDashboardService.FinancialOverview overview = adminDashboardService.getFinancialOverview(startDate, endDate);

        return ResponseEntity.ok(overview);
    }

    @GetMapping("/games/statistics")
    public ResponseEntity<AdminDashboardService.GameStatistics> getGameStatistics(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("GET /admin/dashboard/games/statistics?startDate={}&endDate={}", startDate, endDate);

        AdminDashboardService.GameStatistics stats = adminDashboardService.getGameStatistics(startDate, endDate);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/compliance/overview")
    public ResponseEntity<AdminDashboardService.ComplianceOverview> getComplianceOverview() {
        log.info("GET /admin/dashboard/compliance/overview");

        AdminDashboardService.ComplianceOverview overview = adminDashboardService.getComplianceOverview();

        return ResponseEntity.ok(overview);
    }
}
