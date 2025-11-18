package com.casino.user.repository;

import com.casino.user.entity.AmlAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmlAlertRepository extends JpaRepository<AmlAlert, String> {

    List<AmlAlert> findByUserId(String userId);

    List<AmlAlert> findByStatus(AmlAlert.AlertStatus status);

    List<AmlAlert> findBySeverity(AmlAlert.AlertSeverity severity);

    List<AmlAlert> findByAssignedTo(String assignedTo);

    List<AmlAlert> findByStatusAndSeverity(AmlAlert.AlertStatus status, AmlAlert.AlertSeverity severity);

    List<AmlAlert> findByUserIdOrderByCreatedAtDesc(String userId);

    Long countByStatus(AmlAlert.AlertStatus status);

    Long countBySeverity(AmlAlert.AlertSeverity severity);
}
