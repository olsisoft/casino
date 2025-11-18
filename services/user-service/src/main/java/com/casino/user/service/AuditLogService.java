package com.casino.user.service;

import com.casino.user.entity.AdminUser;
import com.casino.user.entity.AuditLog;
import com.casino.user.repository.AdminUserRepository;
import com.casino.user.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AdminUserRepository adminUserRepository;

    /**
     * Log an admin action
     */
    @Transactional
    public AuditLog log(String adminUserId,
                       AuditLog.ActionType action,
                       String entityType,
                       String entityId,
                       String description,
                       String oldValue,
                       String newValue,
                       AuditLog.Severity severity) {

        String adminUsername = adminUserRepository.findById(adminUserId)
            .map(AdminUser::getUsername)
            .orElse("Unknown");

        AuditLog auditLog = AuditLog.builder()
            .adminUserId(adminUserId)
            .adminUsername(adminUsername)
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .description(description)
            .oldValue(oldValue)
            .newValue(newValue)
            .severity(severity != null ? severity : AuditLog.Severity.INFO)
            .build();

        return auditLogRepository.save(auditLog);
    }

    /**
     * Log with IP and user agent
     */
    @Transactional
    public AuditLog logWithContext(String adminUserId,
                                   AuditLog.ActionType action,
                                   String entityType,
                                   String entityId,
                                   String description,
                                   String ipAddress,
                                   String userAgent,
                                   AuditLog.Severity severity) {

        String adminUsername = adminUserRepository.findById(adminUserId)
            .map(AdminUser::getUsername)
            .orElse("Unknown");

        AuditLog auditLog = AuditLog.builder()
            .adminUserId(adminUserId)
            .adminUsername(adminUsername)
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .description(description)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .severity(severity != null ? severity : AuditLog.Severity.INFO)
            .build();

        return auditLogRepository.save(auditLog);
    }

    /**
     * Get recent audit logs
     */
    public List<AuditLog> getRecentLogs(int limit) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(
            PageRequest.of(0, limit)
        );
    }

    /**
     * Get logs by admin user
     */
    public List<AuditLog> getLogsByAdmin(String adminUserId, int limit) {
        return auditLogRepository.findByAdminUserId(
            adminUserId,
            PageRequest.of(0, limit)
        );
    }

    /**
     * Get logs by entity
     */
    public List<AuditLog> getLogsByEntity(String entityType, String entityId, int limit) {
        return auditLogRepository.findByEntityTypeAndEntityId(
            entityType,
            entityId,
            PageRequest.of(0, limit)
        );
    }

    /**
     * Get logs by action type
     */
    public List<AuditLog> getLogsByAction(AuditLog.ActionType action, int limit) {
        return auditLogRepository.findByAction(
            action,
            PageRequest.of(0, limit)
        );
    }

    /**
     * Get logs by time period
     */
    public List<AuditLog> getLogsByPeriod(LocalDateTime start, LocalDateTime end, int limit) {
        return auditLogRepository.findByCreatedAtBetween(
            start,
            end,
            PageRequest.of(0, limit)
        );
    }

    /**
     * Get critical severity logs
     */
    public List<AuditLog> getCriticalLogs(int limit) {
        return auditLogRepository.findBySeverity(
            AuditLog.Severity.CRITICAL,
            PageRequest.of(0, limit)
        );
    }
}
