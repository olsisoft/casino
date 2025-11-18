package com.casino.user.controller;

import com.casino.user.entity.KycVerification;
import com.casino.user.service.KycService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    @PostMapping("/submit")
    public ResponseEntity<KycVerification> submitKyc(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody KycService.KycSubmissionRequest request
    ) {
        log.info("POST /kyc/submit - userId: {}, level: {}", userId, request.getLevel());

        KycVerification kyc = kycService.submitKyc(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(kyc);
    }

    @GetMapping("/my/status")
    public ResponseEntity<KycVerification> getMyKycStatus(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /kyc/my/status - userId: {}", userId);

        KycVerification kyc = kycService.getKycStatus(userId);

        return ResponseEntity.ok(kyc);
    }

    @GetMapping("/my/verified")
    public ResponseEntity<VerifiedResponse> checkIfVerified(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /kyc/my/verified - userId: {}", userId);

        boolean verified = kycService.isUserVerified(userId);

        return ResponseEntity.ok(new VerifiedResponse(verified));
    }

    // Admin endpoints

    @GetMapping("/admin/pending")
    public ResponseEntity<List<KycVerification>> getPendingVerifications() {
        log.info("GET /kyc/admin/pending");

        List<KycVerification> pending = kycService.getPendingVerifications();

        return ResponseEntity.ok(pending);
    }

    @PostMapping("/admin/{kycId}/verify")
    public ResponseEntity<KycVerification> verifyKyc(
        @PathVariable String kycId,
        @RequestHeader("X-User-Id") String adminUserId
    ) {
        log.info("POST /kyc/admin/{}/verify - adminUserId: {}", kycId, adminUserId);

        KycVerification kyc = kycService.verifyKyc(kycId, adminUserId);

        return ResponseEntity.ok(kyc);
    }

    @PostMapping("/admin/{kycId}/reject")
    public ResponseEntity<KycVerification> rejectKyc(
        @PathVariable String kycId,
        @RequestHeader("X-User-Id") String adminUserId,
        @Valid @RequestBody RejectRequest request
    ) {
        log.info("POST /kyc/admin/{}/reject - adminUserId: {}", kycId, adminUserId);

        KycVerification kyc = kycService.rejectKyc(kycId, request.getReason(), adminUserId);

        return ResponseEntity.ok(kyc);
    }

    @GetMapping("/admin/statistics")
    public ResponseEntity<KycService.KycStatistics> getStatistics() {
        log.info("GET /kyc/admin/statistics");

        KycService.KycStatistics stats = kycService.getStatistics();

        return ResponseEntity.ok(stats);
    }

    @Data
    public static class VerifiedResponse {
        private Boolean verified;

        public VerifiedResponse(Boolean verified) {
            this.verified = verified;
        }
    }

    @Data
    public static class RejectRequest {
        private String reason;
    }
}
