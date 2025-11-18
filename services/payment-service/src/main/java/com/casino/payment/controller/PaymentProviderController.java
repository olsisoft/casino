package com.casino.payment.controller;

import com.casino.payment.dto.PaymentIntentRequest;
import com.casino.payment.dto.PaymentIntentResponse;
import com.casino.payment.dto.PayoutRequest;
import com.casino.payment.dto.PayoutResponse;
import com.casino.payment.service.PaymentProviderService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Payment Provider Controller
 * REST API for payment provider management
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/providers")
@RequiredArgsConstructor
public class PaymentProviderController {

    private final PaymentProviderService paymentProviderService;

    /**
     * GET /api/payment/providers
     * Get all enabled payment providers
     */
    @GetMapping
    public ResponseEntity<List<String>> getEnabledProviders() {
        return ResponseEntity.ok(paymentProviderService.getEnabledProviders());
    }

    /**
     * GET /api/payment/providers/{provider}
     * Get provider capabilities
     */
    @GetMapping("/{provider}")
    public ResponseEntity<Map<String, Object>> getProviderCapabilities(@PathVariable String provider) {
        try {
            Map<String, Object> capabilities = paymentProviderService.getProviderCapabilities(provider);
            return ResponseEntity.ok(capabilities);
        } catch (Exception e) {
            log.error("Error getting provider capabilities", e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/payment/providers/{provider}/deposit
     * Create deposit payment intent
     */
    @PostMapping("/{provider}/deposit")
    public ResponseEntity<PaymentIntentResponse> createDeposit(
        @PathVariable String provider,
        @RequestHeader("Authorization") String token,
        @RequestBody DepositRequest request
    ) {
        try {
            String userId = extractUserIdFromToken(token);

            PaymentIntentRequest intentRequest = PaymentIntentRequest.builder()
                .userId(userId)
                .transactionId(generateTransactionId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .returnUrl(request.getReturnUrl())
                .build();

            PaymentIntentResponse response = paymentProviderService.createPaymentIntent(
                provider,
                intentRequest
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error creating deposit", e);
            return ResponseEntity.badRequest().body(
                PaymentIntentResponse.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build()
            );
        }
    }

    /**
     * POST /api/payment/providers/{provider}/withdraw
     * Create withdrawal payout
     */
    @PostMapping("/{provider}/withdraw")
    public ResponseEntity<PayoutResponse> createWithdrawal(
        @PathVariable String provider,
        @RequestHeader("Authorization") String token,
        @RequestBody WithdrawalRequest request
    ) {
        try {
            String userId = extractUserIdFromToken(token);

            PayoutRequest payoutRequest = PayoutRequest.builder()
                .userId(userId)
                .transactionId(generateTransactionId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .payoutMethod(request.getPayoutMethod())
                .destination(request.getDestination())
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .routingNumber(request.getRoutingNumber())
                .iban(request.getIban())
                .swiftCode(request.getSwiftCode())
                .build();

            PayoutResponse response = paymentProviderService.processPayout(
                provider,
                payoutRequest
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error creating withdrawal", e);
            return ResponseEntity.badRequest().body(
                PayoutResponse.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build()
            );
        }
    }

    /**
     * POST /api/payment/providers/{provider}/fees
     * Calculate fees for deposit
     */
    @PostMapping("/{provider}/fees")
    public ResponseEntity<Map<String, Object>> calculateFees(
        @PathVariable String provider,
        @RequestBody FeeCalculationRequest request
    ) {
        try {
            Map<String, Object> fees = paymentProviderService.calculateDepositFees(
                provider,
                request.getAmount(),
                request.getCurrency()
            );
            return ResponseEntity.ok(fees);
        } catch (Exception e) {
            log.error("Error calculating fees", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/payment/providers/recommended
     * Get recommended provider for user
     */
    @GetMapping("/recommended")
    public ResponseEntity<RecommendedProviderResponse> getRecommendedProvider(
        @RequestParam(required = false, defaultValue = "USD") String currency,
        @RequestParam(required = false, defaultValue = "US") String region,
        @RequestParam(required = false) String preferredMethod
    ) {
        String provider = paymentProviderService.getRecommendedProvider(
            currency, region, preferredMethod
        );

        if (provider == null) {
            return ResponseEntity.notFound().build();
        }

        RecommendedProviderResponse response = new RecommendedProviderResponse();
        response.setProvider(provider);
        response.setReason("Best match for your region and preferences");

        return ResponseEntity.ok(response);
    }

    private String extractUserIdFromToken(String token) {
        // TODO: Implement JWT token extraction
        return "user-123";
    }

    private String generateTransactionId() {
        return "TXN_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 10000);
    }

    @Data
    public static class DepositRequest {
        private BigDecimal amount;
        private String currency;
        private String paymentMethod; // For crypto: BTC, ETH, etc.
        private String returnUrl;
    }

    @Data
    public static class WithdrawalRequest {
        private BigDecimal amount;
        private String currency;
        private String payoutMethod;
        private String destination;    // Email, crypto address, etc.
        private String bankName;
        private String accountNumber;
        private String routingNumber;
        private String iban;
        private String swiftCode;
    }

    @Data
    public static class FeeCalculationRequest {
        private BigDecimal amount;
        private String currency;
    }

    @Data
    public static class RecommendedProviderResponse {
        private String provider;
        private String reason;
    }
}
