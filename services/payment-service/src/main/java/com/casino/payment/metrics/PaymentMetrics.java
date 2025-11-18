package com.casino.payment.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Custom metrics for Payment Service
 * Tracks payment transactions and provider performance
 */
@Component
@RequiredArgsConstructor
public class PaymentMetrics {

    private final MeterRegistry meterRegistry;

    /**
     * Record payment transaction
     */
    public void recordTransaction(String provider, String type, String status, BigDecimal amount, String currency) {
        // Transaction counter
        Counter.builder("payment_transactions_total")
            .tag("provider", provider)
            .tag("type", type) // deposit, withdrawal
            .tag("status", status) // success, failed, pending
            .tag("currency", currency)
            .description("Total payment transactions")
            .register(meterRegistry)
            .increment();

        // Transaction amount
        meterRegistry.summary("payment_transaction_amount")
            .record(amount.doubleValue());

        // Failed transactions
        if ("failed".equals(status)) {
            Counter.builder("payment_failures_total")
                .tag("provider", provider)
                .tag("type", type)
                .description("Failed payment transactions")
                .register(meterRegistry)
                .increment();
        }
    }

    /**
     * Record payment processing time
     */
    public void recordProcessingTime(String provider, String type, Duration duration) {
        Timer.builder("payment_processing_duration_seconds")
            .tag("provider", provider)
            .tag("type", type)
            .description("Payment processing time")
            .register(meterRegistry)
            .record(duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Record payment fees
     */
    public void recordFees(String provider, BigDecimal feeAmount) {
        meterRegistry.summary("payment_fees_collected")
            .record(feeAmount.doubleValue());

        Counter.builder("payment_fees_total")
            .tag("provider", provider)
            .description("Total fees collected")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record payment method usage
     */
    public void recordPaymentMethod(String provider, String method) {
        Counter.builder("payment_method_usage_total")
            .tag("provider", provider)
            .tag("method", method) // card, bank_transfer, crypto, etc.
            .description("Payment method usage")
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record cryptocurrency payment
     */
    public void recordCryptoPayment(String cryptocurrency, String type, BigDecimal amount) {
        Counter.builder("payment_crypto_transactions_total")
            .tag("currency", cryptocurrency)
            .tag("type", type)
            .description("Cryptocurrency transactions")
            .register(meterRegistry)
            .increment();

        meterRegistry.summary("payment_crypto_amount")
            .record(amount.doubleValue());
    }

    /**
     * Record payment verification
     */
    public void recordVerification(String provider, boolean success, Duration duration) {
        Counter.builder("payment_verifications_total")
            .tag("provider", provider)
            .tag("result", success ? "success" : "failed")
            .description("Payment verifications")
            .register(meterRegistry)
            .increment();

        Timer.builder("payment_verification_duration_seconds")
            .tag("provider", provider)
            .description("Payment verification time")
            .register(meterRegistry)
            .record(duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Record chargeback
     */
    public void recordChargeback(String provider, BigDecimal amount) {
        Counter.builder("payment_chargebacks_total")
            .tag("provider", provider)
            .description("Payment chargebacks")
            .register(meterRegistry)
            .increment();

        meterRegistry.summary("payment_chargeback_amount")
            .record(amount.doubleValue());
    }

    /**
     * Record refund
     */
    public void recordRefund(String provider, BigDecimal amount, String reason) {
        Counter.builder("payment_refunds_total")
            .tag("provider", provider)
            .tag("reason", reason)
            .description("Payment refunds")
            .register(meterRegistry)
            .increment();

        meterRegistry.summary("payment_refund_amount")
            .record(amount.doubleValue());
    }

    /**
     * Record provider availability
     */
    public void recordProviderAvailability(String provider, boolean available) {
        meterRegistry.gauge("payment_provider_available",
            available ? 1.0 : 0.0);
    }

    /**
     * Record daily volume
     */
    public void recordDailyVolume(BigDecimal depositVolume, BigDecimal withdrawalVolume) {
        meterRegistry.gauge("payment_daily_deposit_volume", depositVolume.doubleValue());
        meterRegistry.gauge("payment_daily_withdrawal_volume", withdrawalVolume.doubleValue());
    }

    /**
     * Record pending withdrawals
     */
    public void recordPendingWithdrawals(int count, BigDecimal totalAmount) {
        meterRegistry.gauge("payment_pending_withdrawals_count", count);
        meterRegistry.gauge("payment_pending_withdrawals_amount", totalAmount.doubleValue());
    }
}
