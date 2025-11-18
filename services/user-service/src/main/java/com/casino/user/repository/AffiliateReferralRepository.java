package com.casino.user.repository;

import com.casino.user.entity.AffiliateReferral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AffiliateReferralRepository extends JpaRepository<AffiliateReferral, String> {

    Optional<AffiliateReferral> findByReferredUserId(String referredUserId);

    List<AffiliateReferral> findByAffiliateId(String affiliateId);

    List<AffiliateReferral> findByAffiliateIdAndStatus(String affiliateId, AffiliateReferral.ReferralStatus status);

    Long countByAffiliateId(String affiliateId);

    Long countByAffiliateIdAndStatus(String affiliateId, AffiliateReferral.ReferralStatus status);

    List<AffiliateReferral> findByAffiliateIdAndCreatedAtBetween(
        String affiliateId,
        LocalDateTime start,
        LocalDateTime end
    );

    boolean existsByReferredUserId(String referredUserId);
}
