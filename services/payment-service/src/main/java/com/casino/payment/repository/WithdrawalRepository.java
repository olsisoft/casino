package com.casino.payment.repository;

import com.casino.payment.entity.Withdrawal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WithdrawalRepository extends JpaRepository<Withdrawal, String> {

    List<Withdrawal> findByUserIdOrderByCreatedAtDesc(String userId);

    Page<Withdrawal> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    List<Withdrawal> findByStatusOrderByCreatedAtAsc(Withdrawal.WithdrawalStatus status);

    Page<Withdrawal> findByStatusOrderByCreatedAtAsc(
        Withdrawal.WithdrawalStatus status,
        Pageable pageable
    );

    @Query("SELECT w FROM Withdrawal w WHERE w.status = :status " +
           "AND w.createdAt < :cutoffTime ORDER BY w.createdAt ASC")
    List<Withdrawal> findPendingWithdrawals(
        @Param("status") Withdrawal.WithdrawalStatus status,
        @Param("cutoffTime") LocalDateTime cutoffTime
    );

    @Query("SELECT SUM(w.amount) FROM Withdrawal w WHERE w.userId = :userId " +
           "AND w.status = 'COMPLETED'")
    BigDecimal getTotalWithdrawnByUser(@Param("userId") String userId);

    @Query("SELECT SUM(w.amount) FROM Withdrawal w WHERE w.userId = :userId " +
           "AND w.status IN ('PENDING_REVIEW', 'APPROVED', 'PROCESSING')")
    BigDecimal getPendingWithdrawalAmountByUser(@Param("userId") String userId);

    @Query("SELECT COUNT(w) FROM Withdrawal w WHERE w.userId = :userId " +
           "AND w.createdAt BETWEEN :startDate AND :endDate")
    long countWithdrawalsByUserAndDateRange(
        @Param("userId") String userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    long countByUserIdAndStatus(String userId, Withdrawal.WithdrawalStatus status);
}
