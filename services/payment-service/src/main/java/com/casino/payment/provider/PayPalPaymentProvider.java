package com.casino.payment.provider;

import com.casino.payment.dto.PaymentIntentRequest;
import com.casino.payment.dto.PaymentIntentResponse;
import com.casino.payment.dto.PayoutRequest;
import com.casino.payment.dto.PayoutResponse;
import com.casino.payment.exception.PaymentException;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import com.paypal.payouts.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * PayPal Payment Provider
 * Handles PayPal payments and payouts
 */
@Slf4j
@Service
public class PayPalPaymentProvider implements PaymentProvider {

    @Value("${paypal.client.id:}")
    private String clientId;

    @Value("${paypal.client.secret:}")
    private String clientSecret;

    @Value("${paypal.mode:sandbox}")
    private String mode; // sandbox or live

    private PayPalHttpClient client;

    @PostConstruct
    public void init() {
        if (clientId != null && !clientId.isEmpty() && clientSecret != null && !clientSecret.isEmpty()) {
            PayPalEnvironment environment;
            if ("live".equalsIgnoreCase(mode)) {
                environment = new PayPalEnvironment.Live(clientId, clientSecret);
            } else {
                environment = new PayPalEnvironment.Sandbox(clientId, clientSecret);
            }
            this.client = new PayPalHttpClient(environment);
            log.info("PayPal payment provider initialized in {} mode", mode);
        } else {
            log.warn("PayPal credentials not configured");
        }
    }

    @Override
    public String getProviderName() {
        return "PAYPAL";
    }

    @Override
    public boolean isEnabled() {
        return client != null;
    }

    /**
     * Create PayPal order for deposit
     */
    @Override
    public PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request) {
        try {
            // Create order request
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.checkoutPaymentIntent("CAPTURE");

            // Amount
            AmountWithBreakdown amountWithBreakdown = new AmountWithBreakdown()
                .currencyCode(request.getCurrency())
                .value(request.getAmount().toString());

            // Purchase unit
            PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest()
                .amountWithBreakdown(amountWithBreakdown)
                .description("Casino deposit - User: " + request.getUserId())
                .customId(request.getTransactionId());

            List<PurchaseUnitRequest> purchaseUnits = new ArrayList<>();
            purchaseUnits.add(purchaseUnitRequest);
            orderRequest.purchaseUnits(purchaseUnits);

            // Application context (return URLs)
            ApplicationContext applicationContext = new ApplicationContext()
                .returnUrl(request.getReturnUrl() != null ? request.getReturnUrl() : "https://casino.com/payment/success")
                .cancelUrl("https://casino.com/payment/cancel");
            orderRequest.applicationContext(applicationContext);

            // Create order
            OrdersCreateRequest ordersCreateRequest = new OrdersCreateRequest();
            ordersCreateRequest.requestBody(orderRequest);

            HttpResponse<Order> response = client.execute(ordersCreateRequest);
            Order order = response.result();

            // Get approval URL
            String approvalUrl = order.links().stream()
                .filter(link -> "approve".equals(link.rel()))
                .findFirst()
                .map(LinkDescription::href)
                .orElse(null);

            log.info("PayPal order created - ID: {}, Amount: {}, User: {}",
                order.id(), request.getAmount(), request.getUserId());

            return PaymentIntentResponse.builder()
                .success(true)
                .paymentIntentId(order.id())
                .status(order.status())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .redirectUrl(approvalUrl)
                .build();

        } catch (IOException e) {
            log.error("PayPal order creation failed", e);
            throw new PaymentException("Failed to create PayPal order: " + e.getMessage());
        }
    }

    /**
     * Capture PayPal order
     */
    public PaymentIntentResponse captureOrder(String orderId) {
        try {
            OrdersCaptureRequest ordersCaptureRequest = new OrdersCaptureRequest(orderId);
            HttpResponse<Order> response = client.execute(ordersCaptureRequest);
            Order order = response.result();

            // Extract amount
            PurchaseUnit purchaseUnit = order.purchaseUnits().get(0);
            BigDecimal amount = new BigDecimal(purchaseUnit.amountWithBreakdown().value());

            log.info("PayPal order captured - ID: {}, Status: {}", orderId, order.status());

            return PaymentIntentResponse.builder()
                .success(true)
                .paymentIntentId(order.id())
                .status(order.status())
                .amount(amount)
                .currency(purchaseUnit.amountWithBreakdown().currencyCode())
                .build();

        } catch (IOException e) {
            log.error("PayPal order capture failed", e);
            throw new PaymentException("Failed to capture PayPal order: " + e.getMessage());
        }
    }

    /**
     * Process PayPal payout (withdrawal)
     */
    @Override
    public PayoutResponse processPayout(PayoutRequest request) {
        try {
            // Create payout batch
            CreatePayoutRequest payoutRequest = new CreatePayoutRequest();

            // Sender batch header
            SenderBatchHeader senderBatchHeader = new SenderBatchHeader()
                .senderBatchId("batch_" + System.currentTimeMillis())
                .emailSubject("You have a payout from Casino Platform")
                .emailMessage("You have received a payout! Thank you for playing.");
            payoutRequest.senderBatchHeader(senderBatchHeader);

            // Payout item
            Currency amount = new Currency()
                .currency(request.getCurrency())
                .value(request.getAmount().toString());

            PayoutItem item = new PayoutItem()
                .recipientType("EMAIL")
                .receiver(request.getDestination()) // PayPal email
                .amount(amount)
                .note("Casino withdrawal")
                .senderItemId(request.getTransactionId());

            List<PayoutItem> items = new ArrayList<>();
            items.add(item);
            payoutRequest.items(items);

            // Execute payout
            PayoutsPostRequest payoutsPostRequest = new PayoutsPostRequest();
            payoutsPostRequest.requestBody(payoutRequest);

            HttpResponse<CreatePayoutResponse> response = client.execute(payoutsPostRequest);
            CreatePayoutResponse payoutResponse = response.result();

            log.info("PayPal payout created - Batch ID: {}, Amount: {}, User: {}",
                payoutResponse.batchHeader().payoutBatchId(),
                request.getAmount(), request.getUserId());

            return PayoutResponse.builder()
                .success(true)
                .payoutId(payoutResponse.batchHeader().payoutBatchId())
                .status(payoutResponse.batchHeader().batchStatus())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .build();

        } catch (IOException e) {
            log.error("PayPal payout failed", e);
            throw new PaymentException("Failed to process PayPal payout: " + e.getMessage());
        }
    }

    /**
     * Get payout status
     */
    public String getPayoutStatus(String payoutBatchId) {
        try {
            PayoutsGetRequest payoutsGetRequest = new PayoutsGetRequest(payoutBatchId);
            HttpResponse<PayoutBatch> response = client.execute(payoutsGetRequest);
            return response.result().batchHeader().batchStatus();
        } catch (IOException e) {
            log.error("Failed to get PayPal payout status", e);
            throw new PaymentException("Failed to get payout status: " + e.getMessage());
        }
    }
}
