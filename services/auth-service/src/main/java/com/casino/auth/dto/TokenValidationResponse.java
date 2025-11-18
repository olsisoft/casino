package com.casino.auth.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenValidationResponse {

    private boolean valid;
    private String userId;
    private String username;
    private String email;
    private String role;
    private String message;
}
