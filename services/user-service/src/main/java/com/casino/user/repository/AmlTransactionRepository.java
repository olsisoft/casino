package com.casino.user.repository;

import com.casino.user.entity.AmlTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AmlTransactionRepository extends JpaRepository<AmlTransaction, String> {

    List<AmlTransaction> findByUserId(String userId);

    List<AmlTransaction> findByUserIdAndCreatedAtAfter(String userId, LocalDateTime after);

    List<AmlTransaction> findByStatus(AmlTransaction.AmlStatus status);

    List<AmlTransaction> findByRiskLevel(AmlTransaction.RiskLevel riskLevel);

    List<AmlTransaction> findByUserIdOrderByCreatedAtDesc(String userId);

    @Query("SELECT COUNT(t) FROM AmlTransaction t WHERE t.userId = :userId AND t.createdAt >= :since")
    Long countByUserIdSince(String userId, LocalDateTime since);

    @Query("SELECT SUM(t.amount) FROM AmlTransaction t WHERE t.userId = :userId AND t.createdAt >= :since")
    BigDecimal sumAmountByUserIdSince(String userId, LocalDateTime since);

    Long countByStatusAndRiskLevel(AmlTransaction.AmlStatus status, AmlTransaction.RiskLevel riskLevel);
}
