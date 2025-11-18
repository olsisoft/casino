package com.casino.payment.provider;

import com.casino.payment.dto.PaymentIntentRequest;
import com.casino.payment.dto.PaymentIntentResponse;
import com.casino.payment.dto.PayoutRequest;
import com.casino.payment.dto.PayoutResponse;

/**
 * Payment Provider Interface
 * All payment providers must implement this interface
 */
public interface PaymentProvider {

    /**
     * Get provider name
     */
    String getProviderName();

    /**
     * Check if provider is enabled and configured
     */
    boolean isEnabled();

    /**
     * Create a payment intent for deposit
     */
    PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request);

    /**
     * Process a payout (withdrawal)
     */
    PayoutResponse processPayout(PayoutRequest request);
}
