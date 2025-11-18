package com.casino.user.controller;

import com.casino.user.entity.Bonus;
import com.casino.user.service.BonusService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/bonuses")
@RequiredArgsConstructor
public class BonusController {

    private final BonusService bonusService;

    @GetMapping
    public ResponseEntity<List<Bonus>> getUserBonuses(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /bonuses - userId: {}", userId);
        List<Bonus> bonuses = bonusService.getUserBonuses(userId);
        return ResponseEntity.ok(bonuses);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Bonus>> getActiveBonuses(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /bonuses/active - userId: {}", userId);
        List<Bonus> bonuses = bonusService.getActiveBonuses(userId);
        return ResponseEntity.ok(bonuses);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Bonus>> getPendingBonuses(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /bonuses/pending - userId: {}", userId);
        List<Bonus> bonuses = bonusService.getPendingBonuses(userId);
        return ResponseEntity.ok(bonuses);
    }

    @PostMapping("/{bonusId}/activate")
    public ResponseEntity<Bonus> activateBonus(
        @PathVariable String bonusId,
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("POST /bonuses/{}/activate - userId: {}", bonusId, userId);
        Bonus bonus = bonusService.activateBonus(bonusId, userId);
        return ResponseEntity.ok(bonus);
    }

    @PostMapping("/{bonusId}/cancel")
    public ResponseEntity<Void> cancelBonus(
        @PathVariable String bonusId,
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("POST /bonuses/{}/cancel - userId: {}", bonusId, userId);
        bonusService.cancelBonus(bonusId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/balance")
    public ResponseEntity<BonusBalanceResponse> getBonusBalance(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /bonuses/balance - userId: {}", userId);

        BigDecimal totalBonus = bonusService.getTotalActiveBonusBalance(userId);
        BigDecimal remainingWager = bonusService.getTotalRemainingWagerRequirement(userId);
        boolean hasActiveWagering = bonusService.hasActiveWageringRequirements(userId);

        BonusBalanceResponse response = new BonusBalanceResponse(
            totalBonus,
            remainingWager,
            hasActiveWagering
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/record-wager")
    public ResponseEntity<Void> recordWagering(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody RecordWagerRequest request
    ) {
        log.info("POST /bonuses/record-wager - userId: {}, amount: {}", userId, request.getAmount());
        bonusService.recordWagering(userId, request.getAmount());
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class BonusBalanceResponse {
        private BigDecimal totalBonusBalance;
        private BigDecimal remainingWagerRequirement;
        private Boolean hasActiveWagering;

        public BonusBalanceResponse(BigDecimal totalBonusBalance, BigDecimal remainingWagerRequirement, Boolean hasActiveWagering) {
            this.totalBonusBalance = totalBonusBalance;
            this.remainingWagerRequirement = remainingWagerRequirement;
            this.hasActiveWagering = hasActiveWagering;
        }
    }

    @Data
    public static class RecordWagerRequest {
        @NotNull
        private BigDecimal amount;
    }
}
