package com.casino.user.controller;

import com.casino.user.entity.CashbackRecord;
import com.casino.user.service.CashbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/cashback")
@RequiredArgsConstructor
public class CashbackController {

    private final CashbackService cashbackService;

    @GetMapping
    public ResponseEntity<List<CashbackRecord>> getUserCashbacks(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /cashback - userId: {}", userId);
        List<CashbackRecord> cashbacks = cashbackService.getUserCashbacks(userId);
        return ResponseEntity.ok(cashbacks);
    }

    @GetMapping("/claimable")
    public ResponseEntity<List<CashbackRecord>> getClaimableCashbacks(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /cashback/claimable - userId: {}", userId);
        List<CashbackRecord> cashbacks = cashbackService.getClaimableCashbacks(userId);
        return ResponseEntity.ok(cashbacks);
    }

    @PostMapping("/{cashbackId}/claim")
    public ResponseEntity<CashbackRecord> claimCashback(
        @PathVariable String cashbackId,
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("POST /cashback/{}/claim - userId: {}", cashbackId, userId);

        CashbackRecord cashback = cashbackService.claimCashback(cashbackId, userId);

        return ResponseEntity.ok(cashback);
    }

    @GetMapping("/total-claimable")
    public ResponseEntity<TotalClaimableResponse> getTotalClaimable(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /cashback/total-claimable - userId: {}", userId);

        BigDecimal totalClaimable = cashbackService.getTotalClaimableCashback(userId);

        return ResponseEntity.ok(new TotalClaimableResponse(totalClaimable));
    }

    @GetMapping("/stats")
    public ResponseEntity<CashbackService.CashbackStats> getUserCashbackStats(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /cashback/stats - userId: {}", userId);

        CashbackService.CashbackStats stats = cashbackService.getUserCashbackStats(userId);

        return ResponseEntity.ok(stats);
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class TotalClaimableResponse {
        private BigDecimal totalClaimable;
    }
}
