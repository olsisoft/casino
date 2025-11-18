package com.casino.user.repository;

import com.casino.user.entity.Affiliate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AffiliateRepository extends JpaRepository<Affiliate, String> {

    Optional<Affiliate> findByUserId(String userId);

    Optional<Affiliate> findByAffiliateCode(String affiliateCode);

    List<Affiliate> findByStatus(Affiliate.AffiliateStatus status);

    List<Affiliate> findByTier(Affiliate.AffiliateTier tier);

    boolean existsByUserId(String userId);

    boolean existsByAffiliateCode(String affiliateCode);

    @Query("SELECT a FROM Affiliate a WHERE a.isActive = true ORDER BY a.totalEarnings DESC")
    List<Affiliate> findTopAffiliates();

    Long countByStatus(Affiliate.AffiliateStatus status);
}
