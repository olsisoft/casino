package com.casino.user.repository;

import com.casino.user.entity.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, String> {

    Optional<PromoCode> findByCode(String code);

    Optional<PromoCode> findByCodeAndActiveTrue(String code);

    List<PromoCode> findByActiveTrue();

    @Query("SELECT p FROM PromoCode p WHERE p.active = true AND " +
           "(p.validFrom IS NULL OR p.validFrom <= :now) AND " +
           "(p.validUntil IS NULL OR p.validUntil >= :now)")
    List<PromoCode> findValidPromoCodes(LocalDateTime now);

    @Modifying
    @Query("UPDATE PromoCode p SET p.currentUses = p.currentUses + 1 WHERE p.id = :promoCodeId")
    void incrementUsage(String promoCodeId);

    @Modifying
    @Query("UPDATE PromoCode p SET p.active = false WHERE p.validUntil < :now")
    int deactivateExpiredCodes(LocalDateTime now);

    boolean existsByCode(String code);
}
