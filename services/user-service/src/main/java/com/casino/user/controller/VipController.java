package com.casino.user.controller;

import com.casino.user.entity.UserVipStatus;
import com.casino.user.entity.VipTier;
import com.casino.user.service.VipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/vip")
@RequiredArgsConstructor
public class VipController {

    private final VipService vipService;

    @GetMapping("/tiers")
    public ResponseEntity<List<VipTier>> getAllTiers() {
        log.info("GET /vip/tiers");

        List<VipTier> tiers = vipService.getAllTiers();

        return ResponseEntity.ok(tiers);
    }

    @GetMapping("/tiers/{level}")
    public ResponseEntity<VipTier> getTierByLevel(@PathVariable Integer level) {
        log.info("GET /vip/tiers/{}", level);

        VipTier tier = vipService.getTierByLevel(level);

        return ResponseEntity.ok(tier);
    }

    @GetMapping("/my/status")
    public ResponseEntity<UserVipStatus> getMyVipStatus(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /vip/my/status - userId: {}", userId);

        UserVipStatus status = vipService.getUserVipStatus(userId);

        return ResponseEntity.ok(status);
    }

    @PostMapping("/my/initialize")
    public ResponseEntity<UserVipStatus> initializeMyVipStatus(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("POST /vip/my/initialize - userId: {}", userId);

        UserVipStatus status = vipService.initializeVipStatus(userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(status);
    }

    @GetMapping("/my/benefits")
    public ResponseEntity<VipService.VipBenefits> getMyBenefits(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /vip/my/benefits - userId: {}", userId);

        VipService.VipBenefits benefits = vipService.getUserBenefits(userId);

        return ResponseEntity.ok(benefits);
    }

    @PostMapping("/my/claim/monthly")
    public ResponseEntity<VipService.ClaimBonusResponse> claimMonthlyBonus(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("POST /vip/my/claim/monthly - userId: {}", userId);

        VipService.ClaimBonusResponse response = vipService.claimMonthlyBonus(userId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/my/claim/weekly")
    public ResponseEntity<VipService.ClaimBonusResponse> claimWeeklyBonus(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("POST /vip/my/claim/weekly - userId: {}", userId);

        VipService.ClaimBonusResponse response = vipService.claimWeeklyBonus(userId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/my/claim/birthday")
    public ResponseEntity<VipService.ClaimBonusResponse> claimBirthdayBonus(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("POST /vip/my/claim/birthday - userId: {}", userId);

        VipService.ClaimBonusResponse response = vipService.claimBirthdayBonus(userId);

        return ResponseEntity.ok(response);
    }

    // Admin endpoints

    @PostMapping("/admin/tiers")
    public ResponseEntity<VipTier> createTier(
        @Valid @RequestBody VipTier tier
    ) {
        log.info("POST /vip/admin/tiers - level: {}", tier.getLevel());

        VipTier created = vipService.createTier(tier);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/admin/tiers/{tierId}")
    public ResponseEntity<VipTier> updateTier(
        @PathVariable String tierId,
        @Valid @RequestBody VipTier tier
    ) {
        log.info("PUT /vip/admin/tiers/{}", tierId);

        VipTier updated = vipService.updateTier(tierId, tier);

        return ResponseEntity.ok(updated);
    }
}
