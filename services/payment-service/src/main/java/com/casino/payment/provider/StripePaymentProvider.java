package com.casino.payment.provider;

import com.casino.payment.dto.PaymentIntentRequest;
import com.casino.payment.dto.PaymentIntentResponse;
import com.casino.payment.dto.PayoutRequest;
import com.casino.payment.dto.PayoutResponse;
import com.casino.payment.exception.PaymentException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Payout;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PayoutCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Stripe Payment Provider
 * Handles credit/debit card payments via Stripe API
 */
@Slf4j
@Service
public class StripePaymentProvider implements PaymentProvider {

    @Value("${stripe.api.key:}")
    private String apiKey;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isEmpty()) {
            Stripe.apiKey = apiKey;
            log.info("Stripe payment provider initialized");
        } else {
            log.warn("Stripe API key not configured");
        }
    }

    @Override
    public String getProviderName() {
        return "STRIPE";
    }

    @Override
    public boolean isEnabled() {
        return apiKey != null && !apiKey.isEmpty();
    }

    /**
     * Create a payment intent for deposit
     */
    @Override
    public PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request) {
        try {
            // Convert amount to cents (Stripe uses smallest currency unit)
            long amountInCents = request.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

            // Create payment intent params
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(request.getCurrency().toLowerCase())
                .setDescription("Casino deposit - User: " + request.getUserId())
                .putMetadata("user_id", request.getUserId())
                .putMetadata("transaction_id", request.getTransactionId())
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                )
                .build();

            // Create payment intent
            PaymentIntent intent = PaymentIntent.create(params);

            log.info("Stripe payment intent created - ID: {}, Amount: {}, User: {}",
                intent.getId(), request.getAmount(), request.getUserId());

            return PaymentIntentResponse.builder()
                .success(true)
                .paymentIntentId(intent.getId())
                .clientSecret(intent.getClientSecret())
                .status(intent.getStatus())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .build();

        } catch (StripeException e) {
            log.error("Stripe payment intent creation failed", e);
            throw new PaymentException("Failed to create payment intent: " + e.getMessage());
        }
    }

    /**
     * Confirm payment intent
     */
    public PaymentIntentResponse confirmPaymentIntent(String paymentIntentId) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            PaymentIntent confirmedIntent = intent.confirm();

            BigDecimal amount = BigDecimal.valueOf(confirmedIntent.getAmount())
                .divide(BigDecimal.valueOf(100));

            return PaymentIntentResponse.builder()
                .success(true)
                .paymentIntentId(confirmedIntent.getId())
                .status(confirmedIntent.getStatus())
                .amount(amount)
                .currency(confirmedIntent.getCurrency().toUpperCase())
                .build();

        } catch (StripeException e) {
            log.error("Stripe payment confirmation failed", e);
            throw new PaymentException("Failed to confirm payment: " + e.getMessage());
        }
    }

    /**
     * Get payment intent status
     */
    public String getPaymentStatus(String paymentIntentId) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            return intent.getStatus();
        } catch (StripeException e) {
            log.error("Failed to retrieve payment intent", e);
            throw new PaymentException("Failed to get payment status: " + e.getMessage());
        }
    }

    /**
     * Process payout (withdrawal)
     */
    @Override
    public PayoutResponse processPayout(PayoutRequest request) {
        try {
            // Convert amount to cents
            long amountInCents = request.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

            // Create payout params
            PayoutCreateParams params = PayoutCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(request.getCurrency().toLowerCase())
                .setDescription("Casino withdrawal - User: " + request.getUserId())
                .putMetadata("user_id", request.getUserId())
                .putMetadata("transaction_id", request.getTransactionId())
                .build();

            // Create payout
            Payout payout = Payout.create(params);

            log.info("Stripe payout created - ID: {}, Amount: {}, User: {}",
                payout.getId(), request.getAmount(), request.getUserId());

            return PayoutResponse.builder()
                .success(true)
                .payoutId(payout.getId())
                .status(payout.getStatus())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .estimatedArrival(payout.getArrivalDate())
                .build();

        } catch (StripeException e) {
            log.error("Stripe payout creation failed", e);
            throw new PaymentException("Failed to create payout: " + e.getMessage());
        }
    }

    /**
     * Cancel payment intent
     */
    public void cancelPaymentIntent(String paymentIntentId) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            intent.cancel();
            log.info("Stripe payment intent cancelled - ID: {}", paymentIntentId);
        } catch (StripeException e) {
            log.error("Failed to cancel payment intent", e);
            throw new PaymentException("Failed to cancel payment: " + e.getMessage());
        }
    }

    /**
     * Create refund
     */
    public void refundPayment(String paymentIntentId, BigDecimal amount) {
        try {
            long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

            Map<String, Object> params = new HashMap<>();
            params.put("payment_intent", paymentIntentId);
            params.put("amount", amountInCents);

            com.stripe.model.Refund.create(params);
            log.info("Stripe refund created - Payment: {}, Amount: {}", paymentIntentId, amount);
        } catch (StripeException e) {
            log.error("Failed to create refund", e);
            throw new PaymentException("Failed to refund payment: " + e.getMessage());
        }
    }

    /**
     * Verify webhook signature
     */
    public boolean verifyWebhookSignature(String payload, String sigHeader) {
        try {
            com.stripe.model.Event event = com.stripe.webhook.Webhook.constructEvent(
                payload, sigHeader, webhookSecret
            );
            return true;
        } catch (Exception e) {
            log.error("Webhook signature verification failed", e);
            return false;
        }
    }
}
