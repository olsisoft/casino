package com.casino.user.repository;

import com.casino.user.entity.AffiliateCommission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AffiliateCommissionRepository extends JpaRepository<AffiliateCommission, String> {

    List<AffiliateCommission> findByAffiliateId(String affiliateId);

    List<AffiliateCommission> findByAffiliateIdAndStatus(
        String affiliateId,
        AffiliateCommission.CommissionStatus status
    );

    List<AffiliateCommission> findByAffiliateIdAndPeriodStartBetween(
        String affiliateId,
        LocalDate start,
        LocalDate end
    );

    List<AffiliateCommission> findByStatus(AffiliateCommission.CommissionStatus status);

    @Query("SELECT SUM(c.amount) FROM AffiliateCommission c WHERE c.affiliateId = :affiliateId AND c.status = :status")
    BigDecimal sumByAffiliateIdAndStatus(String affiliateId, AffiliateCommission.CommissionStatus status);

    @Query("SELECT SUM(c.amount) FROM AffiliateCommission c WHERE c.affiliateId = :affiliateId")
    BigDecimal sumByAffiliateId(String affiliateId);
}
