package com.casino.user.repository;

import com.casino.user.entity.AuditLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    List<AuditLog> findByAdminUserId(String adminUserId, Pageable pageable);

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId, Pageable pageable);

    List<AuditLog> findByAction(AuditLog.ActionType action, Pageable pageable);

    List<AuditLog> findBySeverity(AuditLog.Severity severity, Pageable pageable);

    List<AuditLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Long countByAdminUserId(String adminUserId);

    Long countByAction(AuditLog.ActionType action);
}
