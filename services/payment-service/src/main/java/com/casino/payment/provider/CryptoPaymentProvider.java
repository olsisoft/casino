package com.casino.payment.provider;

import com.casino.payment.dto.PaymentIntentRequest;
import com.casino.payment.dto.PaymentIntentResponse;
import com.casino.payment.dto.PayoutRequest;
import com.casino.payment.dto.PayoutResponse;
import com.casino.payment.exception.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Cryptocurrency Payment Provider
 * Supports Bitcoin, Ethereum, USDT, and other cryptocurrencies
 * Uses third-party crypto payment gateway (e.g., Coinbase Commerce, BitPay, or NOWPayments)
 */
@Slf4j
@Service
public class CryptoPaymentProvider implements PaymentProvider {

    @Value("${crypto.api.key:}")
    private String apiKey;

    @Value("${crypto.api.url:https://api.nowpayments.io/v1}")
    private String apiUrl;

    @Value("${crypto.ipn.secret:}")
    private String ipnSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isEmpty()) {
            log.info("Crypto payment provider initialized");
        } else {
            log.warn("Crypto payment API key not configured");
        }
    }

    @Override
    public String getProviderName() {
        return "CRYPTO";
    }

    @Override
    public boolean isEnabled() {
        return apiKey != null && !apiKey.isEmpty();
    }

    /**
     * Create crypto payment for deposit
     */
    @Override
    public PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request) {
        try {
            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("price_amount", request.getAmount().toString());
            requestBody.put("price_currency", request.getCurrency().toUpperCase());
            requestBody.put("pay_currency", request.getPaymentMethod().toUpperCase()); // BTC, ETH, USDT, etc.
            requestBody.put("ipn_callback_url", "https://casino.com/api/webhooks/crypto");
            requestBody.put("order_id", request.getTransactionId());
            requestBody.put("order_description", "Casino deposit - User: " + request.getUserId());

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Create payment
            ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + "/payment",
                HttpMethod.POST,
                entity,
                Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new PaymentException("Empty response from crypto payment gateway");
            }

            String paymentId = (String) responseBody.get("payment_id");
            String paymentStatus = (String) responseBody.get("payment_status");
            String payAddress = (String) responseBody.get("pay_address");
            BigDecimal payAmount = new BigDecimal(responseBody.get("pay_amount").toString());
            String payCurrency = (String) responseBody.get("pay_currency");

            log.info("Crypto payment created - ID: {}, Currency: {}, Amount: {}, Address: {}",
                paymentId, payCurrency, payAmount, payAddress);

            return PaymentIntentResponse.builder()
                .success(true)
                .paymentIntentId(paymentId)
                .status(paymentStatus)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .redirectUrl("https://casino.com/payment/crypto/" + paymentId) // Custom payment page
                .build();

        } catch (Exception e) {
            log.error("Crypto payment creation failed", e);
            throw new PaymentException("Failed to create crypto payment: " + e.getMessage());
        }
    }

    /**
     * Get payment status
     */
    public Map<String, Object> getPaymentStatus(String paymentId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-key", apiKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + "/payment/" + paymentId,
                HttpMethod.GET,
                entity,
                Map.class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to get crypto payment status", e);
            throw new PaymentException("Failed to get payment status: " + e.getMessage());
        }
    }

    /**
     * Process crypto payout (withdrawal)
     */
    @Override
    public PayoutResponse processPayout(PayoutRequest request) {
        try {
            // Prepare payout request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("withdrawals", new Object[]{
                Map.of(
                    "address", request.getDestination(), // Crypto wallet address
                    "currency", request.getCurrency().toLowerCase(), // btc, eth, usdt, etc.
                    "amount", request.getAmount().toString(),
                    "ipn_callback_url", "https://casino.com/api/webhooks/crypto-payout"
                )
            });

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Create payout
            ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + "/payout",
                HttpMethod.POST,
                entity,
                Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new PaymentException("Empty response from crypto payout");
            }

            String payoutId = (String) responseBody.get("id");
            String status = (String) responseBody.get("status");

            log.info("Crypto payout created - ID: {}, Currency: {}, Amount: {}, Address: {}",
                payoutId, request.getCurrency(), request.getAmount(), request.getDestination());

            return PayoutResponse.builder()
                .success(true)
                .payoutId(payoutId)
                .status(status)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .build();

        } catch (Exception e) {
            log.error("Crypto payout failed", e);
            throw new PaymentException("Failed to process crypto payout: " + e.getMessage());
        }
    }

    /**
     * Verify IPN (Instant Payment Notification) signature
     */
    public boolean verifyIpnSignature(String payload, String signature) {
        try {
            // Implement HMAC signature verification
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA512");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                ipnSecret.getBytes(), "HmacSHA512"
            );
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes());

            String calculatedSignature = new String(
                org.apache.commons.codec.binary.Hex.encodeHex(hash)
            );

            return calculatedSignature.equals(signature);
        } catch (Exception e) {
            log.error("IPN signature verification failed", e);
            return false;
        }
    }

    /**
     * Get supported cryptocurrencies
     */
    public String[] getSupportedCurrencies() {
        return new String[]{
            "BTC", "ETH", "USDT", "USDC", "LTC", "BCH", "XRP", "BNB", "DOGE", "TRX"
        };
    }

    /**
     * Get minimum deposit amounts for each currency
     */
    public Map<String, BigDecimal> getMinimumDeposits() {
        Map<String, BigDecimal> minimums = new HashMap<>();
        minimums.put("BTC", new BigDecimal("0.0001"));
        minimums.put("ETH", new BigDecimal("0.001"));
        minimums.put("USDT", new BigDecimal("10"));
        minimums.put("USDC", new BigDecimal("10"));
        minimums.put("LTC", new BigDecimal("0.01"));
        minimums.put("BCH", new BigDecimal("0.01"));
        minimums.put("XRP", new BigDecimal("1"));
        minimums.put("BNB", new BigDecimal("0.01"));
        minimums.put("DOGE", new BigDecimal("10"));
        minimums.put("TRX", new BigDecimal("10"));
        return minimums;
    }
}
