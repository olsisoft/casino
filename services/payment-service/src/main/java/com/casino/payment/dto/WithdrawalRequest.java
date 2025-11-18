package com.casino.payment.dto;

import com.casino.payment.entity.Withdrawal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawalRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "10.0", message = "Minimum withdrawal is 10.00")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotNull(message = "Withdrawal method is required")
    private Withdrawal.WithdrawalMethod method;

    // Bank transfer details
    private String bankAccountNumber;
    private String bankRoutingNumber;
    private String bankAccountHolderName;

    // PayPal details
    private String paypalEmail;

    // Card ID for card withdrawals
    private String paymentMethodId;
}
