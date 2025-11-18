package com.casino.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Email or username is required")
    private String identifier; // Can be email or username

    @NotBlank(message = "Password is required")
    private String password;

    private String twoFactorCode; // Optional, only if 2FA is enabled
}
