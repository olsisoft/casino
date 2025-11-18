package com.casino.user.controller;

import com.casino.user.entity.Affiliate;
import com.casino.user.entity.AffiliateCommission;
import com.casino.user.entity.AffiliateReferral;
import com.casino.user.service.AffiliateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/affiliates")
@RequiredArgsConstructor
public class AffiliateController {

    private final AffiliateService affiliateService;

    @PostMapping("/apply")
    public ResponseEntity<Affiliate> applyForAffiliate(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody ApplyRequest request
    ) {
        log.info("POST /affiliates/apply - userId: {}", userId);

        Affiliate affiliate = affiliateService.applyForAffiliate(
            userId,
            request.getWebsiteUrl(),
            request.getTrafficSource()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(affiliate);
    }

    @GetMapping("/my/dashboard")
    public ResponseEntity<AffiliateService.AffiliateDashboard> getMyDashboard(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /affiliates/my/dashboard - userId: {}", userId);

        AffiliateService.AffiliateDashboard dashboard = affiliateService.getAffiliateDashboard(userId);

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/my/referrals")
    public ResponseEntity<List<AffiliateReferral>> getMyReferrals(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /affiliates/my/referrals - userId: {}", userId);

        List<AffiliateReferral> referrals = affiliateService.getAffiliateReferrals(userId);

        return ResponseEntity.ok(referrals);
    }

    @GetMapping("/my/commissions")
    public ResponseEntity<List<AffiliateCommission>> getMyCommissions(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /affiliates/my/commissions - userId: {}", userId);

        List<AffiliateCommission> commissions = affiliateService.getAffiliateCommissions(userId);

        return ResponseEntity.ok(commissions);
    }

    @PostMapping("/my/payout")
    public ResponseEntity<AffiliateService.PayoutResponse> requestPayout(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("POST /affiliates/my/payout - userId: {}", userId);

        AffiliateService.PayoutResponse response = affiliateService.requestPayout(userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Affiliate> getAffiliateByCode(@PathVariable String code) {
        log.info("GET /affiliates/code/{}", code);

        Affiliate affiliate = affiliateService.getAffiliateByCode(code);

        return ResponseEntity.ok(affiliate);
    }

    // Admin endpoints

    @PostMapping("/admin/{affiliateId}/approve")
    public ResponseEntity<Affiliate> approveAffiliate(@PathVariable String affiliateId) {
        log.info("POST /affiliates/admin/{}/approve", affiliateId);

        Affiliate affiliate = affiliateService.approveAffiliate(affiliateId);

        return ResponseEntity.ok(affiliate);
    }

    @Data
    public static class ApplyRequest {
        private String websiteUrl;
        @NotBlank
        private String trafficSource;
    }
}
