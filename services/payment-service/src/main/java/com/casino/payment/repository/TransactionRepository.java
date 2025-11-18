package com.casino.payment.repository;

import com.casino.payment.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByUserIdOrderByCreatedAtDesc(String userId);

    Page<Transaction> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    List<Transaction> findByUserIdAndTypeOrderByCreatedAtDesc(
        String userId,
        Transaction.TransactionType type
    );

    List<Transaction> findByUserIdAndStatusOrderByCreatedAtDesc(
        String userId,
        Transaction.TransactionStatus status
    );

    Optional<Transaction> findByStripePaymentIntentId(String stripePaymentIntentId);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId " +
           "AND t.type = :type AND t.status = :status " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findByUserIdTypeAndStatus(
        @Param("userId") String userId,
        @Param("type") Transaction.TransactionType type,
        @Param("status") Transaction.TransactionStatus status
    );

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.userId = :userId " +
           "AND t.type = :type AND t.status = 'COMPLETED'")
    BigDecimal getTotalAmountByUserAndType(
        @Param("userId") String userId,
        @Param("type") Transaction.TransactionType type
    );

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.userId = :userId " +
           "AND t.type = 'DEPOSIT' AND t.status = 'COMPLETED' " +
           "AND t.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalDepositsByUserAndDateRange(
        @Param("userId") String userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT t FROM Transaction t WHERE t.status = :status " +
           "AND t.createdAt < :cutoffTime ORDER BY t.createdAt ASC")
    List<Transaction> findStaleTransactions(
        @Param("status") Transaction.TransactionStatus status,
        @Param("cutoffTime") LocalDateTime cutoffTime
    );

    long countByUserIdAndType(String userId, Transaction.TransactionType type);
}
