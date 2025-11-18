package com.casino.game.dto;

import com.casino.game.entity.GameSession;
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
public class StartSessionRequest {

    @NotBlank(message = "Game code is required")
    private String gameCode;

    @NotNull(message = "Starting balance is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Starting balance must be positive")
    private BigDecimal startingBalance;

    @NotNull(message = "Balance type is required")
    private GameSession.BalanceType balanceType;
}
