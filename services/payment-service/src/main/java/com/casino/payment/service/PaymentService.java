package com.casino.payment.service;

import com.casino.payment.dto.*;
import com.casino.payment.entity.PaymentMethod;
import com.casino.payment.entity.Transaction;
import com.casino.payment.entity.Withdrawal;
import com.casino.payment.exception.InsufficientFundsException;
import com.casino.payment.exception.PaymentException;
import com.casino.payment.exception.PaymentMethodNotFoundException;
import com.casino.payment.repository.PaymentMethodRepository;
import com.casino.payment.repository.TransactionRepository;
import com.casino.payment.repository.WithdrawalRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.BalanceTransaction;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final TransactionRepository transactionRepository;
    private final WithdrawalRepository withdrawalRepository;
    private final StripeService stripeService;
    private final UserServiceClient userServiceClient; // Feign client

    private static final BigDecimal PLATFORM_FEE_PERCENTAGE = new BigDecimal("0.02"); // 2%
    private static final BigDecimal MIN_DEPOSIT = new BigDecimal("1.00");
    private static final BigDecimal MAX_DEPOSIT = new BigDecimal("10000.00");
    private static final BigDecimal MIN_WITHDRAWAL = new BigDecimal("10.00");

    /**
     * Add a payment method for a user
     */
    @Transactional
    public PaymentMethodDto addPaymentMethod(
        String userId,
        String email,
        String name,
        AddPaymentMethodRequest request
    ) {
        try {
            // Create or get Stripe customer
            Customer customer = stripeService.createOrGetCustomer(userId, email, name);

            // Attach payment method to customer
            com.stripe.model.PaymentMethod stripePaymentMethod = stripeService.attachPaymentMethod(
                request.getStripePaymentMethodId(),
                customer.getId()
            );

            // Check if already exists
            if (paymentMethodRepository.existsByUserIdAndStripePaymentMethodId(
                    userId, request.getStripePaymentMethodId())) {
                throw new PaymentException("Payment method already added");
            }

            // Clear default if setting this as default
            if (Boolean.TRUE.equals(request.getSetAsDefault())) {
                paymentMethodRepository.clearDefaultForUser(userId);
            }

            // Save payment method
            PaymentMethod paymentMethod = PaymentMethod.builder()
                .userId(userId)
                .type(PaymentMethod.PaymentMethodType.CARD)
                .stripePaymentMethodId(stripePaymentMethod.getId())
                .stripeCustomerId(customer.getId())
                .cardBrand(stripePaymentMethod.getCard().getBrand())
                .cardLast4(stripePaymentMethod.getCard().getLast4())
                .cardExpMonth(stripePaymentMethod.getCard().getExpMonth().intValue())
                .cardExpYear(stripePaymentMethod.getCard().getExpYear().intValue())
                .isDefault(Boolean.TRUE.equals(request.getSetAsDefault()))
                .build();

            paymentMethod = paymentMethodRepository.save(paymentMethod);

            log.info("Added payment method {} for user {}", paymentMethod.getId(), userId);

            return toPaymentMethodDto(paymentMethod);

        } catch (StripeException e) {
            log.error("Stripe error adding payment method: {}", e.getMessage());
            throw new PaymentException("Failed to add payment method: " + e.getMessage(), e);
        }
    }

    /**
     * Get all payment methods for a user
     */
    public List<PaymentMethodDto> getPaymentMethods(String userId) {
        return paymentMethodRepository.findByUserIdAndIsActiveTrue(userId).stream()
            .map(this::toPaymentMethodDto)
            .collect(Collectors.toList());
    }

    /**
     * Remove a payment method
     */
    @Transactional
    public void removePaymentMethod(String userId, String paymentMethodId) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
            .orElseThrow(() -> new PaymentMethodNotFoundException("Payment method not found"));

        if (!paymentMethod.getUserId().equals(userId)) {
            throw new PaymentException("Payment method does not belong to user");
        }

        try {
            stripeService.detachPaymentMethod(paymentMethod.getStripePaymentMethodId());
        } catch (StripeException e) {
            log.warn("Failed to detach payment method from Stripe: {}", e.getMessage());
        }

        paymentMethodRepository.delete(paymentMethod);
        log.info("Removed payment method {} for user {}", paymentMethodId, userId);
    }

    /**
     * Process a deposit
     */
    @Transactional
    public DepositResponse processDeposit(String userId, DepositRequest request) {
        // Validate amount
        if (request.getAmount().compareTo(MIN_DEPOSIT) < 0) {
            throw new PaymentException("Deposit amount below minimum: " + MIN_DEPOSIT);
        }
        if (request.getAmount().compareTo(MAX_DEPOSIT) > 0) {
            throw new PaymentException("Deposit amount exceeds maximum: " + MAX_DEPOSIT);
        }

        // Get payment method
        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
            .orElseThrow(() -> new PaymentMethodNotFoundException("Payment method not found"));

        if (!paymentMethod.getUserId().equals(userId)) {
            throw new PaymentException("Payment method does not belong to user");
        }

        try {
            // Calculate fees
            BigDecimal platformFee = request.getAmount()
                .multiply(PLATFORM_FEE_PERCENTAGE)
                .setScale(2, RoundingMode.HALF_UP);

            // Create transaction record
            Transaction transaction = Transaction.builder()
                .userId(userId)
                .type(Transaction.TransactionType.DEPOSIT)
                .status(Transaction.TransactionStatus.PROCESSING)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethodType(paymentMethod.getType())
                .paymentMethodId(paymentMethod.getId())
                .platformFee(platformFee)
                .description("Deposit via " + paymentMethod.getCardBrand() + " ending in " + paymentMethod.getCardLast4())
                .build();

            transaction = transactionRepository.save(transaction);

            // Create Stripe payment intent
            PaymentIntent intent = stripeService.createPaymentIntent(
                request.getAmount(),
                request.getCurrency(),
                paymentMethod.getStripeCustomerId(),
                paymentMethod.getStripePaymentMethodId(),
                userId
            );

            // Update transaction with Stripe info
            transaction.setStripePaymentIntentId(intent.getId());

            if ("succeeded".equals(intent.getStatus())) {
                transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
                transaction.setStripeChargeId(intent.getCharges().getData().get(0).getId());

                // Get fee info
                BalanceTransaction balanceTransaction = stripeService.getBalanceTransaction(
                    intent.getCharges().getData().get(0).getBalanceTransaction()
                );
                BigDecimal stripeFee = new BigDecimal(balanceTransaction.getFee())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                transaction.setPaymentProcessorFee(stripeFee);

                BigDecimal netAmount = request.getAmount()
                    .subtract(platformFee)
                    .subtract(stripeFee);
                transaction.setNetAmount(netAmount);

                // Update user balance
                userServiceClient.addRealBalance(userId, netAmount);

                // Update payment method last used
                paymentMethodRepository.updateLastUsed(paymentMethod.getId(), LocalDateTime.now());

                log.info("Deposit completed: userId={}, amount={}, transactionId={}",
                    userId, request.getAmount(), transaction.getId());

            } else if ("requires_action".equals(intent.getStatus())) {
                transaction.setStatus(Transaction.TransactionStatus.PENDING);
            } else {
                transaction.setStatus(Transaction.TransactionStatus.FAILED);
                transaction.setFailureReason("Payment intent status: " + intent.getStatus());
            }

            transaction = transactionRepository.save(transaction);

            return DepositResponse.builder()
                .transactionId(transaction.getId())
                .status(transaction.getStatus())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .stripePaymentIntentId(intent.getId())
                .clientSecret(intent.getClientSecret())
                .requiresAction("requires_action".equals(intent.getStatus()))
                .nextActionUrl(intent.getNextAction() != null ?
                    intent.getNextAction().getRedirectToUrl().getUrl() : null)
                .build();

        } catch (StripeException e) {
            log.error("Stripe error processing deposit: {}", e.getMessage());
            throw new PaymentException("Failed to process deposit: " + e.getMessage(), e);
        }
    }

    /**
     * Request a withdrawal
     */
    @Transactional
    public Withdrawal requestWithdrawal(String userId, WithdrawalRequest request) {
        // Validate amount
        if (request.getAmount().compareTo(MIN_WITHDRAWAL) < 0) {
            throw new PaymentException("Withdrawal amount below minimum: " + MIN_WITHDRAWAL);
        }

        // Check user balance
        BigDecimal userBalance = userServiceClient.getRealBalance(userId);
        if (userBalance.compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient balance for withdrawal");
        }

        // Check for pending withdrawals
        BigDecimal pendingAmount = withdrawalRepository.getPendingWithdrawalAmountByUser(userId);
        if (pendingAmount != null && pendingAmount.compareTo(BigDecimal.ZERO) > 0) {
            throw new PaymentException("You have pending withdrawals. Please wait for them to complete.");
        }

        // Create transaction
        Transaction transaction = Transaction.builder()
            .userId(userId)
            .type(Transaction.TransactionType.WITHDRAWAL)
            .status(Transaction.TransactionStatus.PENDING)
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .description("Withdrawal request")
            .build();

        transaction = transactionRepository.save(transaction);

        // Create withdrawal
        Withdrawal withdrawal = Withdrawal.builder()
            .userId(userId)
            .transactionId(transaction.getId())
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .method(request.getMethod())
            .bankAccountNumber(request.getBankAccountNumber())
            .bankRoutingNumber(request.getBankRoutingNumber())
            .bankAccountHolderName(request.getBankAccountHolderName())
            .paypalEmail(request.getPaypalEmail())
            .build();

        withdrawal = withdrawalRepository.save(withdrawal);

        // Lock the amount in user balance
        userServiceClient.lockAmount(userId, request.getAmount());

        log.info("Withdrawal requested: userId={}, amount={}, withdrawalId={}",
            userId, request.getAmount(), withdrawal.getId());

        return withdrawal;
    }

    /**
     * Get transaction history for a user
     */
    public Page<TransactionDto> getTransactionHistory(String userId, Pageable pageable) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map(this::toTransactionDto);
    }

    /**
     * Get a specific transaction
     */
    public TransactionDto getTransaction(String userId, String transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new PaymentException("Transaction not found"));

        if (!transaction.getUserId().equals(userId)) {
            throw new PaymentException("Transaction does not belong to user");
        }

        return toTransactionDto(transaction);
    }

    // DTO Converters

    private PaymentMethodDto toPaymentMethodDto(PaymentMethod paymentMethod) {
        return PaymentMethodDto.builder()
            .id(paymentMethod.getId())
            .type(paymentMethod.getType())
            .cardBrand(paymentMethod.getCardBrand())
            .cardLast4(paymentMethod.getCardLast4())
            .cardExpMonth(paymentMethod.getCardExpMonth())
            .cardExpYear(paymentMethod.getCardExpYear())
            .paypalEmail(paymentMethod.getPaypalEmail())
            .isDefault(paymentMethod.getIsDefault())
            .isActive(paymentMethod.getIsActive())
            .createdAt(paymentMethod.getCreatedAt())
            .lastUsedAt(paymentMethod.getLastUsedAt())
            .build();
    }

    private TransactionDto toTransactionDto(Transaction transaction) {
        return TransactionDto.builder()
            .id(transaction.getId())
            .type(transaction.getType())
            .status(transaction.getStatus())
            .amount(transaction.getAmount())
            .currency(transaction.getCurrency())
            .paymentMethodType(transaction.getPaymentMethodType())
            .platformFee(transaction.getPlatformFee())
            .paymentProcessorFee(transaction.getPaymentProcessorFee())
            .netAmount(transaction.getNetAmount())
            .description(transaction.getDescription())
            .failureReason(transaction.getFailureReason())
            .createdAt(transaction.getCreatedAt())
            .completedAt(transaction.getCompletedAt())
            .build();
    }
}
