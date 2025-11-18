package com.casino.payment.repository;

import com.casino.payment.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, String> {

    List<PaymentMethod> findByUserIdAndIsActiveTrue(String userId);

    List<PaymentMethod> findByUserId(String userId);

    Optional<PaymentMethod> findByUserIdAndIsDefaultTrue(String userId);

    Optional<PaymentMethod> findByStripePaymentMethodId(String stripePaymentMethodId);

    @Modifying
    @Query("UPDATE PaymentMethod pm SET pm.isDefault = false WHERE pm.userId = :userId")
    int clearDefaultForUser(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE PaymentMethod pm SET pm.lastUsedAt = :lastUsedAt WHERE pm.id = :id")
    int updateLastUsed(@Param("id") String id, @Param("lastUsedAt") LocalDateTime lastUsedAt);

    boolean existsByUserIdAndStripePaymentMethodId(String userId, String stripePaymentMethodId);
}
