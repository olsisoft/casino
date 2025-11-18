package com.casino.payment.controller;

import com.casino.payment.dto.*;
import com.casino.payment.entity.Withdrawal;
import com.casino.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/methods")
    public ResponseEntity<PaymentMethodDto> addPaymentMethod(
        @RequestHeader("X-User-Id") String userId,
        @RequestHeader("X-Email") String email,
        @RequestHeader("X-Username") String username,
        @Valid @RequestBody AddPaymentMethodRequest request
    ) {
        log.info("POST /payments/methods - userId: {}", userId);
        PaymentMethodDto method = paymentService.addPaymentMethod(userId, email, username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(method);
    }

    @GetMapping("/methods")
    public ResponseEntity<List<PaymentMethodDto>> getPaymentMethods(
        @RequestHeader("X-User-Id") String userId
    ) {
        log.info("GET /payments/methods - userId: {}", userId);
        List<PaymentMethodDto> methods = paymentService.getPaymentMethods(userId);
        return ResponseEntity.ok(methods);
    }

    @DeleteMapping("/methods/{methodId}")
    public ResponseEntity<Void> removePaymentMethod(
        @RequestHeader("X-User-Id") String userId,
        @PathVariable String methodId
    ) {
        log.info("DELETE /payments/methods/{} - userId: {}", methodId, userId);
        paymentService.removePaymentMethod(userId, methodId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/deposit")
    public ResponseEntity<DepositResponse> deposit(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody DepositRequest request
    ) {
        log.info("POST /payments/deposit - userId: {}, amount: {}", userId, request.getAmount());
        DepositResponse response = paymentService.processDeposit(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Withdrawal> withdraw(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody WithdrawalRequest request
    ) {
        log.info("POST /payments/withdraw - userId: {}, amount: {}", userId, request.getAmount());
        Withdrawal withdrawal = paymentService.requestWithdrawal(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(withdrawal);
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionDto>> getTransactions(
        @RequestHeader("X-User-Id") String userId,
        Pageable pageable
    ) {
        log.info("GET /payments/transactions - userId: {}", userId);
        Page<TransactionDto> transactions = paymentService.getTransactionHistory(userId, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<TransactionDto> getTransaction(
        @RequestHeader("X-User-Id") String userId,
        @PathVariable String transactionId
    ) {
        log.info("GET /payments/transactions/{} - userId: {}", transactionId, userId);
        TransactionDto transaction = paymentService.getTransaction(userId, transactionId);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Payment Service is running");
    }
}
