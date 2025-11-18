package com.casino.user.controller;

import com.casino.user.entity.Bonus;
import com.casino.user.entity.PromoCode;
import com.casino.user.service.PromoCodeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/promo-codes")
@RequiredArgsConstructor
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    @GetMapping
    public ResponseEntity<List<PromoCode>> getAllActivePromoCodes() {
        log.info("GET /promo-codes");
        List<PromoCode> promoCodes = promoCodeService.getAllActivePromoCodes();
        return ResponseEntity.ok(promoCodes);
    }

    @GetMapping("/{code}")
    public ResponseEntity<PromoCode> getPromoCode(@PathVariable String code) {
        log.info("GET /promo-codes/{}", code);
        PromoCode promoCode = promoCodeService.getPromoCode(code);
        return ResponseEntity.ok(promoCode);
    }

    @PostMapping("/apply")
    public ResponseEntity<Bonus> applyPromoCode(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody ApplyPromoCodeRequest request
    ) {
        log.info("POST /promo-codes/apply - userId: {}, code: {}", userId, request.getCode());

        Bonus bonus = promoCodeService.applyPromoCode(
            userId,
            request.getCode(),
            request.getDepositAmount()
        );

        return ResponseEntity.ok(bonus);
    }

    @PostMapping("/admin/create")
    public ResponseEntity<PromoCode> createPromoCode(
        @RequestHeader("X-Admin-Id") String adminId,
        @Valid @RequestBody CreatePromoCodeRequest request
    ) {
        log.info("POST /promo-codes/admin/create - admin: {}, code: {}", adminId, request.getCode());

        PromoCode promoCode = PromoCode.builder()
            .code(request.getCode())
            .name(request.getName())
            .description(request.getDescription())
            .promoType(request.getPromoType())
            .bonusAmount(request.getBonusAmount())
            .bonusPercentage(request.getBonusPercentage())
            .maxBonusAmount(request.getMaxBonusAmount())
            .minDepositAmount(request.getMinDepositAmount())
            .wagerMultiplier(request.getWagerMultiplier())
            .maxUses(request.getMaxUses())
            .maxUsesPerUser(request.getMaxUsesPerUser())
            .active(true)
            .validFrom(request.getValidFrom())
            .validUntil(request.getValidUntil())
            .createdBy(adminId)
            .build();

        promoCode = promoCodeService.createPromoCode(promoCode);

        return ResponseEntity.status(HttpStatus.CREATED).body(promoCode);
    }

    @PutMapping("/admin/{promoCodeId}")
    public ResponseEntity<PromoCode> updatePromoCode(
        @PathVariable String promoCodeId,
        @RequestHeader("X-Admin-Id") String adminId,
        @Valid @RequestBody PromoCode updates
    ) {
        log.info("PUT /promo-codes/admin/{} - admin: {}", promoCodeId, adminId);
        PromoCode promoCode = promoCodeService.updatePromoCode(promoCodeId, updates);
        return ResponseEntity.ok(promoCode);
    }

    @DeleteMapping("/admin/{promoCodeId}")
    public ResponseEntity<Void> deactivatePromoCode(
        @PathVariable String promoCodeId,
        @RequestHeader("X-Admin-Id") String adminId
    ) {
        log.info("DELETE /promo-codes/admin/{} - admin: {}", promoCodeId, adminId);
        promoCodeService.deactivatePromoCode(promoCodeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/{promoCodeId}/stats")
    public ResponseEntity<PromoCodeService.PromoCodeStats> getPromoCodeStats(
        @PathVariable String promoCodeId,
        @RequestHeader("X-Admin-Id") String adminId
    ) {
        log.info("GET /promo-codes/admin/{}/stats - admin: {}", promoCodeId, adminId);
        PromoCodeService.PromoCodeStats stats = promoCodeService.getPromoCodeStats(promoCodeId);
        return ResponseEntity.ok(stats);
    }

    @Data
    public static class ApplyPromoCodeRequest {
        @NotBlank
        private String code;

        @NotNull
        private BigDecimal depositAmount;
    }

    @Data
    public static class CreatePromoCodeRequest {
        @NotBlank
        private String code;

        @NotBlank
        private String name;

        private String description;

        @NotNull
        private PromoCode.PromoType promoType;

        private BigDecimal bonusAmount;
        private BigDecimal bonusPercentage;
        private BigDecimal maxBonusAmount;
        private BigDecimal minDepositAmount;

        private Integer wagerMultiplier;
        private Integer maxUses;
        private Integer maxUsesPerUser;

        private java.time.LocalDateTime validFrom;
        private java.time.LocalDateTime validUntil;
    }
}
