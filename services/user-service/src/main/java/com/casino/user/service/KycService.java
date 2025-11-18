package com.casino.user.service;

import com.casino.user.entity.KycVerification;
import com.casino.user.exception.KycException;
import com.casino.user.repository.KycVerificationRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KycService {

    private final KycVerificationRepository kycVerificationRepository;

    /**
     * Submit KYC verification
     */
    @Transactional
    public KycVerification submitKyc(String userId, KycSubmissionRequest request) {
        KycVerification existing = kycVerificationRepository.findByUserId(userId).orElse(null);

        if (existing != null && existing.getStatus() == KycVerification.KycStatus.APPROVED) {
            throw new KycException("KYC already verified for this user");
        }

        KycVerification kyc = existing != null ? existing : new KycVerification();
        kyc.setUserId(userId);
        kyc.setLevel(request.getLevel());
        kyc.setStatus(KycVerification.KycStatus.PENDING);

        // Personal information
        kyc.setFirstName(request.getFirstName());
        kyc.setLastName(request.getLastName());
        kyc.setDateOfBirth(request.getDateOfBirth());
        kyc.setNationality(request.getNationality());
        kyc.setCountryOfResidence(request.getCountryOfResidence());

        // Address information (for Level 2+)
        if (request.getLevel().ordinal() >= KycVerification.KycLevel.LEVEL_2.ordinal()) {
            kyc.setAddressLine1(request.getAddressLine1());
            kyc.setAddressLine2(request.getAddressLine2());
            kyc.setCity(request.getCity());
            kyc.setState(request.getState());
            kyc.setPostalCode(request.getPostalCode());
            kyc.setCountry(request.getCountry());
        }

        // Document information
        kyc.setDocumentType(request.getDocumentType());
        kyc.setDocumentNumber(request.getDocumentNumber());
        kyc.setDocumentIssuingCountry(request.getDocumentIssuingCountry());
        kyc.setDocumentExpiryDate(request.getDocumentExpiryDate());

        // Document uploads
        kyc.setDocumentFrontImageUrl(request.getDocumentFrontImageUrl());
        kyc.setDocumentBackImageUrl(request.getDocumentBackImageUrl());

        // Selfie for Level 3
        if (request.getLevel() == KycVerification.KycLevel.LEVEL_3) {
            kyc.setSelfieImageUrl(request.getSelfieImageUrl());
            kyc.setProofOfAddressImageUrl(request.getProofOfAddressImageUrl());
        }

        // Calculate initial risk score
        kyc.setRiskScore(calculateRiskScore(kyc));
        kyc.setRiskLevel(determineRiskLevel(kyc.getRiskScore()));

        log.info("KYC submitted for user: {} at level: {}", userId, request.getLevel());

        return kycVerificationRepository.save(kyc);
    }

    /**
     * Verify KYC (manual review by admin)
     */
    @Transactional
    public KycVerification verifyKyc(String kycId, String verifiedBy) {
        KycVerification kyc = kycVerificationRepository.findById(kycId)
            .orElseThrow(() -> new KycException("KYC verification not found"));

        kyc.setStatus(KycVerification.KycStatus.APPROVED);
        kyc.setVerifiedBy(verifiedBy);
        kyc.setVerifiedAt(LocalDateTime.now());

        log.info("KYC approved for user: {} by: {}", kyc.getUserId(), verifiedBy);

        return kycVerificationRepository.save(kyc);
    }

    /**
     * Reject KYC
     */
    @Transactional
    public KycVerification rejectKyc(String kycId, String rejectionReason, String rejectedBy) {
        KycVerification kyc = kycVerificationRepository.findById(kycId)
            .orElseThrow(() -> new KycException("KYC verification not found"));

        kyc.setStatus(KycVerification.KycStatus.REJECTED);
        kyc.setRejectionReason(rejectionReason);
        kyc.setRejectedAt(LocalDateTime.now());
        kyc.setVerifiedBy(rejectedBy);

        log.info("KYC rejected for user: {} by: {}", kyc.getUserId(), rejectedBy);

        return kycVerificationRepository.save(kyc);
    }

    /**
     * Get KYC status for user
     */
    public KycVerification getKycStatus(String userId) {
        return kycVerificationRepository.findByUserId(userId)
            .orElseThrow(() -> new KycException("KYC verification not found for user"));
    }

    /**
     * Check if user is verified
     */
    public boolean isUserVerified(String userId) {
        return kycVerificationRepository.findByUserId(userId)
            .map(KycVerification::isApproved)
            .orElse(false);
    }

    /**
     * Get all pending KYC verifications (admin)
     */
    public List<KycVerification> getPendingVerifications() {
        return kycVerificationRepository.findByStatus(KycVerification.KycStatus.PENDING);
    }

    /**
     * Calculate risk score based on various factors
     */
    private Integer calculateRiskScore(KycVerification kyc) {
        int score = 0;

        // Age risk
        if (kyc.getDateOfBirth() != null) {
            int age = Period.between(kyc.getDateOfBirth(), LocalDate.now()).getYears();
            if (age < 21) score += 20;
            else if (age < 25) score += 10;
        }

        // Document expiry
        if (kyc.isDocumentExpired()) {
            score += 30;
        } else if (kyc.getDocumentExpiryDate() != null) {
            long daysUntilExpiry = Period.between(LocalDate.now(), kyc.getDocumentExpiryDate()).getDays();
            if (daysUntilExpiry < 30) score += 15;
        }

        // High-risk countries (simplified)
        if (isHighRiskCountry(kyc.getCountryOfResidence())) {
            score += 25;
        }

        // PEP flag
        if (Boolean.TRUE.equals(kyc.getIsPep())) {
            score += 30;
        }

        // Sanctioned
        if (Boolean.TRUE.equals(kyc.getIsSanctioned())) {
            score += 50;
        }

        return Math.min(score, 100);
    }

    /**
     * Determine risk level from score
     */
    private KycVerification.RiskLevel determineRiskLevel(Integer score) {
        if (score >= 75) return KycVerification.RiskLevel.CRITICAL;
        if (score >= 50) return KycVerification.RiskLevel.HIGH;
        if (score >= 25) return KycVerification.RiskLevel.MEDIUM;
        return KycVerification.RiskLevel.LOW;
    }

    /**
     * Check if country is high risk (simplified)
     */
    private boolean isHighRiskCountry(String country) {
        // Simplified - in production, use FATF grey/black list
        return country != null && (
            country.equalsIgnoreCase("XX") // Example high-risk code
        );
    }

    /**
     * Get KYC statistics (admin)
     */
    public KycStatistics getStatistics() {
        Long pending = kycVerificationRepository.countByStatus(KycVerification.KycStatus.PENDING);
        Long approved = kycVerificationRepository.countByStatus(KycVerification.KycStatus.APPROVED);
        Long rejected = kycVerificationRepository.countByStatus(KycVerification.KycStatus.REJECTED);

        return KycStatistics.builder()
            .pendingCount(pending)
            .approvedCount(approved)
            .rejectedCount(rejected)
            .build();
    }

    @Data
    @Builder
    public static class KycSubmissionRequest {
        private KycVerification.KycLevel level;
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private String nationality;
        private String countryOfResidence;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private KycVerification.DocumentType documentType;
        private String documentNumber;
        private String documentIssuingCountry;
        private LocalDate documentExpiryDate;
        private String documentFrontImageUrl;
        private String documentBackImageUrl;
        private String selfieImageUrl;
        private String proofOfAddressImageUrl;
    }

    @Data
    @Builder
    public static class KycStatistics {
        private Long pendingCount;
        private Long approvedCount;
        private Long rejectedCount;
    }
}
