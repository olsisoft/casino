package com.casino.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddPaymentMethodRequest {

    @NotBlank(message = "Stripe payment method ID is required")
    private String stripePaymentMethodId;

    private Boolean setAsDefault;
}
