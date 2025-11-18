package com.casino.user.repository;

import com.casino.user.entity.KycVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KycVerificationRepository extends JpaRepository<KycVerification, String> {

    Optional<KycVerification> findByUserId(String userId);

    List<KycVerification> findByStatus(KycVerification.KycStatus status);

    List<KycVerification> findByLevel(KycVerification.KycLevel level);

    List<KycVerification> findByRiskLevel(KycVerification.RiskLevel riskLevel);

    Long countByStatus(KycVerification.KycStatus status);

    boolean existsByUserId(String userId);
}
