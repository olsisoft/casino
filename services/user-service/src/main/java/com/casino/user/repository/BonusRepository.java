package com.casino.user.repository;

import com.casino.user.entity.Bonus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BonusRepository extends JpaRepository<Bonus, String> {

    List<Bonus> findByUserIdOrderByIssuedAtDesc(String userId);

    List<Bonus> findByUserIdAndStatus(String userId, Bonus.BonusStatus status);

    List<Bonus> findByUserIdAndStatusIn(String userId, List<Bonus.BonusStatus> statuses);

    Optional<Bonus> findByUserIdAndBonusTypeAndStatus(
        String userId,
        Bonus.BonusType bonusType,
        Bonus.BonusStatus status
    );

    @Query("SELECT b FROM Bonus b WHERE b.userId = :userId AND b.status = 'ACTIVE' AND b.isActive = true")
    List<Bonus> findActiveByUserId(String userId);

    @Query("SELECT b FROM Bonus b WHERE b.status = 'ACTIVE' AND b.expiresAt < :now")
    List<Bonus> findExpiredBonuses(LocalDateTime now);

    @Query("SELECT COUNT(b) FROM Bonus b WHERE b.userId = :userId AND b.bonusType = :bonusType")
    Long countByUserIdAndBonusType(String userId, Bonus.BonusType bonusType);

    @Modifying
    @Query("UPDATE Bonus b SET b.wageredAmount = b.wageredAmount + :amount WHERE b.id = :bonusId")
    void addWageredAmount(String bonusId, BigDecimal amount);

    @Modifying
    @Query("UPDATE Bonus b SET b.status = :status, b.completedAt = :completedAt WHERE b.id = :bonusId")
    void updateStatus(String bonusId, Bonus.BonusStatus status, LocalDateTime completedAt);

    @Modifying
    @Query("UPDATE Bonus b SET b.status = 'EXPIRED' WHERE b.status = 'ACTIVE' AND b.expiresAt < :now")
    int expireOldBonuses(LocalDateTime now);

    boolean existsByUserIdAndBonusType(String userId, Bonus.BonusType bonusType);
}
