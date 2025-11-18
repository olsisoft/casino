package com.casino.payment.service;

import com.casino.payment.dto.PaymentIntentRequest;
import com.casino.payment.dto.PaymentIntentResponse;
import com.casino.payment.dto.PayoutRequest;
import com.casino.payment.dto.PayoutResponse;
import com.casino.payment.exception.PaymentException;
import com.casino.payment.provider.CryptoPaymentProvider;
import com.casino.payment.provider.PayPalPaymentProvider;
import com.casino.payment.provider.PaymentProvider;
import com.casino.payment.provider.StripePaymentProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Payment Provider Service
 * Orchestrates all payment providers and routes requests appropriately
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProviderService {

    private final StripePaymentProvider stripeProvider;
    private final PayPalPaymentProvider payPalProvider;
    private final CryptoPaymentProvider cryptoProvider;

    /**
     * Get payment provider by name
     */
    public PaymentProvider getProvider(String providerName) {
        switch (providerName.toUpperCase()) {
            case "STRIPE":
                return stripeProvider;
            case "PAYPAL":
                return payPalProvider;
            case "CRYPTO":
                return cryptoProvider;
            default:
                throw new PaymentException("Unknown payment provider: " + providerName);
        }
    }

    /**
     * Get all enabled payment providers
     */
    public List<String> getEnabledProviders() {
        return List.of(stripeProvider, payPalProvider, cryptoProvider).stream()
            .filter(PaymentProvider::isEnabled)
            .map(PaymentProvider::getProviderName)
            .collect(Collectors.toList());
    }

    /**
     * Check if provider is enabled
     */
    public boolean isProviderEnabled(String providerName) {
        try {
            PaymentProvider provider = getProvider(providerName);
            return provider.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Create payment intent with specified provider
     */
    public PaymentIntentResponse createPaymentIntent(String providerName, PaymentIntentRequest request) {
        PaymentProvider provider = getProvider(providerName);

        if (!provider.isEnabled()) {
            throw new PaymentException("Payment provider " + providerName + " is not enabled");
        }

        log.info("Creating payment intent - Provider: {}, User: {}, Amount: {}",
            providerName, request.getUserId(), request.getAmount());

        return provider.createPaymentIntent(request);
    }

    /**
     * Process payout with specified provider
     */
    public PayoutResponse processPayout(String providerName, PayoutRequest request) {
        PaymentProvider provider = getProvider(providerName);

        if (!provider.isEnabled()) {
            throw new PaymentException("Payment provider " + providerName + " is not enabled");
        }

        log.info("Processing payout - Provider: {}, User: {}, Amount: {}",
            providerName, request.getUserId(), request.getAmount());

        return provider.processPayout(request);
    }

    /**
     * Get provider-specific capabilities
     */
    public Map<String, Object> getProviderCapabilities(String providerName) {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("provider", providerName);
        capabilities.put("enabled", isProviderEnabled(providerName));

        switch (providerName.toUpperCase()) {
            case "STRIPE":
                capabilities.put("supports_cards", true);
                capabilities.put("supports_bank_transfer", true);
                capabilities.put("supports_instant_payout", false);
                capabilities.put("min_deposit", 1.00);
                capabilities.put("max_deposit", 100000.00);
                capabilities.put("deposit_fee_percent", 2.9);
                capabilities.put("deposit_fee_fixed", 0.30);
                capabilities.put("payout_fee", 0.25);
                break;

            case "PAYPAL":
                capabilities.put("supports_cards", true);
                capabilities.put("supports_bank_transfer", true);
                capabilities.put("supports_instant_payout", true);
                capabilities.put("min_deposit", 1.00);
                capabilities.put("max_deposit", 10000.00);
                capabilities.put("deposit_fee_percent", 3.49);
                capabilities.put("deposit_fee_fixed", 0.49);
                capabilities.put("payout_fee", 2.00);
                break;

            case "CRYPTO":
                capabilities.put("supports_bitcoin", true);
                capabilities.put("supports_ethereum", true);
                capabilities.put("supports_stablecoins", true);
                capabilities.put("min_deposit", cryptoProvider.getMinimumDeposits());
                capabilities.put("supported_currencies", cryptoProvider.getSupportedCurrencies());
                capabilities.put("deposit_fee_percent", 1.0);
                capabilities.put("payout_fee_percent", 1.0);
                capabilities.put("network_fee", "varies");
                break;
        }

        return capabilities;
    }

    /**
     * Get recommended provider for user based on preferences
     */
    public String getRecommendedProvider(String currency, String region, String preferredMethod) {
        // Simple recommendation logic - can be enhanced with ML
        if ("crypto".equalsIgnoreCase(preferredMethod) ||
            List.of("BTC", "ETH", "USDT", "USDC").contains(currency.toUpperCase())) {
            return cryptoProvider.isEnabled() ? "CRYPTO" : null;
        }

        if ("paypal".equalsIgnoreCase(preferredMethod)) {
            return payPalProvider.isEnabled() ? "PAYPAL" : null;
        }

        // Default to Stripe for fiat currencies
        if (stripeProvider.isEnabled()) {
            return "STRIPE";
        }

        // Fallback to any enabled provider
        List<String> enabled = getEnabledProviders();
        return enabled.isEmpty() ? null : enabled.get(0);
    }

    /**
     * Calculate total fees for deposit
     */
    public Map<String, Object> calculateDepositFees(String providerName,
                                                     java.math.BigDecimal amount,
                                                     String currency) {
        Map<String, Object> fees = new HashMap<>();
        java.math.BigDecimal feeAmount = java.math.BigDecimal.ZERO;
        java.math.BigDecimal feePercent = java.math.BigDecimal.ZERO;
        java.math.BigDecimal feeFixed = java.math.BigDecimal.ZERO;

        switch (providerName.toUpperCase()) {
            case "STRIPE":
                feePercent = new java.math.BigDecimal("2.9");
                feeFixed = new java.math.BigDecimal("0.30");
                feeAmount = amount.multiply(feePercent.divide(new java.math.BigDecimal("100")))
                    .add(feeFixed);
                break;

            case "PAYPAL":
                feePercent = new java.math.BigDecimal("3.49");
                feeFixed = new java.math.BigDecimal("0.49");
                feeAmount = amount.multiply(feePercent.divide(new java.math.BigDecimal("100")))
                    .add(feeFixed);
                break;

            case "CRYPTO":
                feePercent = new java.math.BigDecimal("1.0");
                feeAmount = amount.multiply(feePercent.divide(new java.math.BigDecimal("100")));
                break;
        }

        fees.put("amount", amount);
        fees.put("fee_percent", feePercent);
        fees.put("fee_fixed", feeFixed);
        fees.put("fee_total", feeAmount.setScale(2, java.math.RoundingMode.HALF_UP));
        fees.put("amount_after_fees",
            amount.subtract(feeAmount).setScale(2, java.math.RoundingMode.HALF_UP));

        return fees;
    }
}
