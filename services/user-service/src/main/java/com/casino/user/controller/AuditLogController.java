package com.casino.user.controller;

import com.casino.user.entity.AuditLog;
import com.casino.user.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/recent")
    public ResponseEntity<List<AuditLog>> getRecentLogs(
        @RequestParam(defaultValue = "100") int limit
    ) {
        log.info("GET /admin/audit-logs/recent?limit={}", limit);

        List<AuditLog> logs = auditLogService.getRecentLogs(limit);

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/by-admin/{adminUserId}")
    public ResponseEntity<List<AuditLog>> getLogsByAdmin(
        @PathVariable String adminUserId,
        @RequestParam(defaultValue = "100") int limit
    ) {
        log.info("GET /admin/audit-logs/by-admin/{}?limit={}", adminUserId, limit);

        List<AuditLog> logs = auditLogService.getLogsByAdmin(adminUserId, limit);

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/by-entity")
    public ResponseEntity<List<AuditLog>> getLogsByEntity(
        @RequestParam String entityType,
        @RequestParam String entityId,
        @RequestParam(defaultValue = "100") int limit
    ) {
        log.info("GET /admin/audit-logs/by-entity?entityType={}&entityId={}&limit={}",
            entityType, entityId, limit);

        List<AuditLog> logs = auditLogService.getLogsByEntity(entityType, entityId, limit);

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/by-action/{action}")
    public ResponseEntity<List<AuditLog>> getLogsByAction(
        @PathVariable AuditLog.ActionType action,
        @RequestParam(defaultValue = "100") int limit
    ) {
        log.info("GET /admin/audit-logs/by-action/{}?limit={}", action, limit);

        List<AuditLog> logs = auditLogService.getLogsByAction(action, limit);

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/by-period")
    public ResponseEntity<List<AuditLog>> getLogsByPeriod(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
        @RequestParam(defaultValue = "100") int limit
    ) {
        log.info("GET /admin/audit-logs/by-period?start={}&end={}&limit={}", start, end, limit);

        List<AuditLog> logs = auditLogService.getLogsByPeriod(start, end, limit);

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/critical")
    public ResponseEntity<List<AuditLog>> getCriticalLogs(
        @RequestParam(defaultValue = "100") int limit
    ) {
        log.info("GET /admin/audit-logs/critical?limit={}", limit);

        List<AuditLog> logs = auditLogService.getCriticalLogs(limit);

        return ResponseEntity.ok(logs);
    }
}
