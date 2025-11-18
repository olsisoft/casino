package com.casino.user.repository;

import com.casino.user.entity.CashbackRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CashbackRepository extends JpaRepository<CashbackRecord, String> {

    List<CashbackRecord> findByUserIdOrderByPeriodStartDesc(String userId);

    List<CashbackRecord> findByUserIdAndStatus(String userId, CashbackRecord.CashbackStatus status);

    Optional<CashbackRecord> findByUserIdAndPeriodStartAndPeriodEnd(
        String userId,
        LocalDate periodStart,
        LocalDate periodEnd
    );

    @Query("SELECT c FROM CashbackRecord c WHERE c.userId = :userId AND c.status = 'CLAIMABLE' AND " +
           "(c.expiresAt IS NULL OR c.expiresAt > :now)")
    List<CashbackRecord> findClaimableByUserId(String userId, LocalDateTime now);

    @Query("SELECT c FROM CashbackRecord c WHERE c.status = 'CLAIMABLE' AND c.expiresAt < :now")
    List<CashbackRecord> findExpiredCashbacks(LocalDateTime now);

    @Modifying
    @Query("UPDATE CashbackRecord c SET c.status = 'CLAIMED', c.claimedAt = :claimedAt WHERE c.id = :cashbackId")
    void markAsClaimed(String cashbackId, LocalDateTime claimedAt);

    @Modifying
    @Query("UPDATE CashbackRecord c SET c.status = 'EXPIRED' WHERE c.status = 'CLAIMABLE' AND c.expiresAt < :now")
    int expireOldCashbacks(LocalDateTime now);

    boolean existsByUserIdAndPeriodStartAndPeriodEnd(String userId, LocalDate periodStart, LocalDate periodEnd);
}
