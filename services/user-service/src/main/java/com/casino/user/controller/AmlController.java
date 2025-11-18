package com.casino.user.controller;

import com.casino.user.entity.AmlAlert;
import com.casino.user.entity.AmlTransaction;
import com.casino.user.service.AmlService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/aml")
@RequiredArgsConstructor
public class AmlController {

    private final AmlService amlService;

    @GetMapping("/my/transactions")
    public ResponseEntity<List<AmlTransaction>> getMyTransactions(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /aml/my/transactions - userId: {}", userId);

        List<AmlTransaction> transactions = amlService.getUserTransactions(userId);

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/my/alerts")
    public ResponseEntity<List<AmlAlert>> getMyAlerts(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /aml/my/alerts - userId: {}", userId);

        List<AmlAlert> alerts = amlService.getUserAlerts(userId);

        return ResponseEntity.ok(alerts);
    }

    // Admin endpoints

    @GetMapping("/admin/flagged")
    public ResponseEntity<List<AmlTransaction>> getFlaggedTransactions() {
        log.info("GET /aml/admin/flagged");

        List<AmlTransaction> flagged = amlService.getFlaggedTransactions();

        return ResponseEntity.ok(flagged);
    }

    @GetMapping("/admin/alerts")
    public ResponseEntity<List<AmlAlert>> getAlerts(
        @RequestParam(required = false) AmlAlert.AlertStatus status
    ) {
        log.info("GET /aml/admin/alerts?status={}", status);

        List<AmlAlert> alerts = status != null ?
            amlService.getAlertsByStatus(status) :
            amlService.getAlertsByStatus(AmlAlert.AlertStatus.NEW);

        return ResponseEntity.ok(alerts);
    }

    @PostMapping("/admin/transactions/{amlTransactionId}/review")
    public ResponseEntity<AmlTransaction> reviewTransaction(
        @PathVariable String amlTransactionId,
        @RequestHeader("X-User-Id") String adminUserId,
        @Valid @RequestBody ReviewRequest request
    ) {
        log.info("POST /aml/admin/transactions/{}/review - adminUserId: {}",
            amlTransactionId, adminUserId);

        AmlTransaction amlTx = amlService.reviewTransaction(
            amlTransactionId,
            request.getDecision(),
            request.getNotes(),
            adminUserId
        );

        return ResponseEntity.ok(amlTx);
    }

    @PostMapping("/admin/alerts/{alertId}/resolve")
    public ResponseEntity<AmlAlert> resolveAlert(
        @PathVariable String alertId,
        @RequestHeader("X-User-Id") String adminUserId,
        @Valid @RequestBody ResolveAlertRequest request
    ) {
        log.info("POST /aml/admin/alerts/{}/resolve - adminUserId: {}", alertId, adminUserId);

        AmlAlert alert = amlService.resolveAlert(
            alertId,
            request.getResolution(),
            request.getNotes(),
            adminUserId
        );

        return ResponseEntity.ok(alert);
    }

    @PostMapping("/admin/transactions/{amlTransactionId}/file-sar")
    public ResponseEntity<AmlTransaction> fileSar(
        @PathVariable String amlTransactionId,
        @RequestHeader("X-User-Id") String adminUserId
    ) {
        log.info("POST /aml/admin/transactions/{}/file-sar - adminUserId: {}",
            amlTransactionId, adminUserId);

        AmlTransaction amlTx = amlService.fileSar(amlTransactionId, adminUserId);

        return ResponseEntity.ok(amlTx);
    }

    @GetMapping("/admin/statistics")
    public ResponseEntity<AmlService.AmlStatistics> getStatistics() {
        log.info("GET /aml/admin/statistics");

        AmlService.AmlStatistics stats = amlService.getStatistics();

        return ResponseEntity.ok(stats);
    }

    @Data
    public static class ReviewRequest {
        private AmlTransaction.ReviewDecision decision;
        private String notes;
    }

    @Data
    public static class ResolveAlertRequest {
        private AmlAlert.AlertResolution resolution;
        private String notes;
    }
}
