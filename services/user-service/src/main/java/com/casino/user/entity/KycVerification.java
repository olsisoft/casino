package com.casino.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_verifications", indexes = {
    @Index(name = "idx_kyc_user", columnList = "userId"),
    @Index(name = "idx_kyc_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycLevel level; // Level of verification

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus status;

    // Personal Information
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String nationality;
    private String countryOfResidence;

    // Address Information
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    // Identity Document
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    private String documentNumber;
    private String documentIssuingCountry;
    private LocalDate documentExpiryDate;

    // Document uploads (URLs or file IDs)
    private String documentFrontImageUrl;
    private String documentBackImageUrl;
    private String selfieImageUrl;
    private String proofOfAddressImageUrl;

    // Verification details
    private String verifiedBy; // Admin user ID who verified
    private LocalDateTime verifiedAt;

    @Column(length = 2000)
    private String rejectionReason;

    private LocalDateTime rejectedAt;

    // Risk scoring
    private Integer riskScore; // 0-100 (higher = riskier)

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    // External verification
    private String externalVerificationProvider; // Third-party KYC provider
    private String externalVerificationId;
    private LocalDateTime externalVerificationDate;

    // Compliance
    private Boolean isPep; // Politically Exposed Person
    private Boolean isSanctioned; // On sanctions list
    private Boolean isHighRisk;

    @Column(length = 2000)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum KycLevel {
        LEVEL_0,    // No verification (limited access)
        LEVEL_1,    // Basic verification (email + phone)
        LEVEL_2,    // Standard verification (ID document)
        LEVEL_3     // Enhanced verification (ID + proof of address + selfie)
    }

    public enum KycStatus {
        NOT_STARTED,
        PENDING,        // Documents submitted, awaiting review
        IN_REVIEW,      // Under manual review
        APPROVED,       // Verified
        REJECTED,       // Rejected
        EXPIRED,        // Verification expired (need re-verification)
        RESUBMISSION_REQUIRED
    }

    public enum DocumentType {
        PASSPORT,
        NATIONAL_ID,
        DRIVERS_LICENSE,
        RESIDENCE_PERMIT
    }

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    /**
     * Check if verification is approved
     */
    public boolean isApproved() {
        return status == KycStatus.APPROVED;
    }

    /**
     * Check if documents are expired
     */
    public boolean isDocumentExpired() {
        if (documentExpiryDate == null) return false;
        return documentExpiryDate.isBefore(LocalDate.now());
    }
}
