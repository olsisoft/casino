package com.casino.user.repository;

import com.casino.user.entity.UserBalance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface UserBalanceRepository extends JpaRepository<UserBalance, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserBalance> findByUserId(String userId);

    @Modifying
    @Query("UPDATE UserBalance ub SET ub.virtualBalance = ub.virtualBalance + :amount WHERE ub.userId = :userId")
    void addVirtualBalance(@Param("userId") String userId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE UserBalance ub SET ub.virtualBalance = ub.virtualBalance - :amount " +
           "WHERE ub.userId = :userId AND ub.virtualBalance >= :amount")
    int deductVirtualBalance(@Param("userId") String userId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE UserBalance ub SET ub.realBalance = ub.realBalance + :amount WHERE ub.userId = :userId")
    void addRealBalance(@Param("userId") String userId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE UserBalance ub SET ub.realBalance = ub.realBalance - :amount " +
           "WHERE ub.userId = :userId AND ub.realBalance >= :amount")
    int deductRealBalance(@Param("userId") String userId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE UserBalance ub SET ub.lockedAmount = ub.lockedAmount + :amount WHERE ub.userId = :userId")
    void lockAmount(@Param("userId") String userId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE UserBalance ub SET ub.lockedAmount = ub.lockedAmount - :amount " +
           "WHERE ub.userId = :userId AND ub.lockedAmount >= :amount")
    void unlockAmount(@Param("userId") String userId, @Param("amount") BigDecimal amount);
}
