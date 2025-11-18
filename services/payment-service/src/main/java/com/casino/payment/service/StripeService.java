package com.casino.payment.service;

import com.casino.payment.config.StripeProperties;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeService {

    private final StripeProperties stripeProperties;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeProperties.getSecretKey();
        log.info("Stripe API initialized");
    }

    /**
     * Create or retrieve a Stripe customer for a user
     */
    public Customer createOrGetCustomer(String userId, String email, String name) throws StripeException {
        // Try to find existing customer by metadata
        CustomerSearchParams searchParams = CustomerSearchParams.builder()
            .setQuery("metadata['userId']:'" + userId + "'")
            .build();

        CustomerSearchResult result = Customer.search(searchParams);

        if (result.getData().size() > 0) {
            return result.getData().get(0);
        }

        // Create new customer
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userId", userId);

        CustomerCreateParams params = CustomerCreateParams.builder()
            .setEmail(email)
            .setName(name)
            .putAllMetadata(metadata)
            .build();

        Customer customer = Customer.create(params);
        log.info("Created Stripe customer {} for user {}", customer.getId(), userId);
        return customer;
    }

    /**
     * Attach a payment method to a customer
     */
    public PaymentMethod attachPaymentMethod(String paymentMethodId, String customerId)
            throws StripeException {
        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);

        PaymentMethodAttachParams params = PaymentMethodAttachParams.builder()
            .setCustomer(customerId)
            .build();

        paymentMethod = paymentMethod.attach(params);
        log.info("Attached payment method {} to customer {}", paymentMethodId, customerId);
        return paymentMethod;
    }

    /**
     * Create a payment intent for a deposit
     */
    public PaymentIntent createPaymentIntent(
        BigDecimal amount,
        String currency,
        String customerId,
        String paymentMethodId,
        String userId
    ) throws StripeException {
        // Stripe expects amount in smallest currency unit (cents for USD)
        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userId", userId);
        metadata.put("type", "deposit");

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(amountInCents)
            .setCurrency(currency.toLowerCase())
            .setCustomer(customerId)
            .setPaymentMethod(paymentMethodId)
            .setConfirm(true)
            .setAutomaticPaymentMethods(
                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                    .setEnabled(true)
                    .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                    .build()
            )
            .putAllMetadata(metadata)
            .build();

        PaymentIntent intent = PaymentIntent.create(params);
        log.info("Created payment intent {} for {} {}", intent.getId(), amount, currency);
        return intent;
    }

    /**
     * Confirm a payment intent (for 3D Secure flows)
     */
    public PaymentIntent confirmPaymentIntent(String paymentIntentId) throws StripeException {
        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

        PaymentIntentConfirmParams params = PaymentIntentConfirmParams.builder()
            .build();

        intent = intent.confirm(params);
        log.info("Confirmed payment intent {}", paymentIntentId);
        return intent;
    }

    /**
     * Retrieve a payment intent
     */
    public PaymentIntent getPaymentIntent(String paymentIntentId) throws StripeException {
        return PaymentIntent.retrieve(paymentIntentId);
    }

    /**
     * Cancel a payment intent
     */
    public PaymentIntent cancelPaymentIntent(String paymentIntentId) throws StripeException {
        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

        PaymentIntentCancelParams params = PaymentIntentCancelParams.builder()
            .build();

        intent = intent.cancel(params);
        log.info("Cancelled payment intent {}", paymentIntentId);
        return intent;
    }

    /**
     * Create a refund
     */
    public Refund createRefund(String chargeId, BigDecimal amount, String reason)
            throws StripeException {
        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

        RefundCreateParams params = RefundCreateParams.builder()
            .setCharge(chargeId)
            .setAmount(amountInCents)
            .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
            .build();

        Refund refund = Refund.create(params);
        log.info("Created refund {} for charge {}", refund.getId(), chargeId);
        return refund;
    }

    /**
     * Create a payout for withdrawal
     */
    public Payout createPayout(BigDecimal amount, String currency) throws StripeException {
        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

        PayoutCreateParams params = PayoutCreateParams.builder()
            .setAmount(amountInCents)
            .setCurrency(currency.toLowerCase())
            .build();

        Payout payout = Payout.create(params);
        log.info("Created payout {} for {} {}", payout.getId(), amount, currency);
        return payout;
    }

    /**
     * Detach a payment method from a customer
     */
    public PaymentMethod detachPaymentMethod(String paymentMethodId) throws StripeException {
        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
        paymentMethod = paymentMethod.detach();
        log.info("Detached payment method {}", paymentMethodId);
        return paymentMethod;
    }

    /**
     * List payment methods for a customer
     */
    public PaymentMethodCollection listPaymentMethods(String customerId, String type)
            throws StripeException {
        PaymentMethodListParams params = PaymentMethodListParams.builder()
            .setCustomer(customerId)
            .setType(PaymentMethodListParams.Type.valueOf(type.toUpperCase()))
            .build();

        return PaymentMethod.list(params);
    }

    /**
     * Get balance transaction details (for fee calculation)
     */
    public BalanceTransaction getBalanceTransaction(String balanceTransactionId)
            throws StripeException {
        return BalanceTransaction.retrieve(balanceTransactionId);
    }

    /**
     * Verify a webhook signature
     */
    public Event constructWebhookEvent(String payload, String sigHeader) throws StripeException {
        return Webhook.constructEvent(
            payload,
            sigHeader,
            stripeProperties.getWebhookSecret()
        );
    }
}
