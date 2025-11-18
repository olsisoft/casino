package com.casino.auth.service;

import com.casino.auth.dto.*;
import com.casino.auth.entity.RefreshToken;
import com.casino.auth.entity.User;
import com.casino.auth.exception.*;
import com.casino.auth.repository.RefreshTokenRepository;
import com.casino.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already in use");
        }

        // Create new user
        User user = User.builder()
            .email(request.getEmail())
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            .build();

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getId());

        // Generate tokens
        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.getIdentifier());

        // Find user by email or username
        User user = userRepository.findByEmailOrUsername(request.getIdentifier())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        // Check if user is active
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new AuthException("Account is " + user.getStatus().name().toLowerCase());
        }

        // Check 2FA if enabled
        if (user.isTwoFactorEnabled()) {
            if (request.getTwoFactorCode() == null) {
                throw new AuthException("2FA code required");
            }
            // TODO: Validate 2FA code (Phase 2)
        }

        // Update last login
        userRepository.updateLastLogin(user.getId(), LocalDateTime.now());

        log.info("User logged in successfully: {}", user.getId());
        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenString) {
        log.info("Refreshing token");

        // Find refresh token
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
            .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        // Validate token
        if (!refreshToken.isValid()) {
            throw new InvalidTokenException("Refresh token is expired or revoked");
        }

        // Find user
        User user = userRepository.findById(refreshToken.getUserId())
            .orElseThrow(() -> new InvalidTokenException("User not found"));

        // Generate new tokens
        return generateAuthResponse(user);
    }

    @Transactional
    public void logout(String userId) {
        log.info("Logging out user: {}", userId);
        refreshTokenRepository.revokeAllUserTokens(userId);
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenString = jwtService.generateRefreshToken(user);

        // Save refresh token
        RefreshToken refreshToken = RefreshToken.builder()
            .userId(user.getId())
            .token(refreshTokenString)
            .expiresAt(LocalDateTime.now().plusDays(7))
            .build();
        refreshTokenRepository.save(refreshToken);

        // Build response
        AuthResponse.UserDto userDto = AuthResponse.UserDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .username(user.getUsername())
            .role(user.getRole().name())
            .status(user.getStatus().name())
            .emailVerified(user.isEmailVerified())
            .twoFactorEnabled(user.isTwoFactorEnabled())
            .build();

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshTokenString)
            .expiresIn(900L) // 15 minutes
            .user(userDto)
            .build();
    }

    public TokenValidationResponse validateToken(String token) {
        try {
            if (!jwtService.validateToken(token)) {
                return TokenValidationResponse.builder()
                    .valid(false)
                    .message("Invalid token")
                    .build();
            }

            var claims = jwtService.getAllClaims(token);
            return TokenValidationResponse.builder()
                .valid(true)
                .userId(claims.getSubject())
                .username((String) claims.get("username"))
                .email((String) claims.get("email"))
                .role((String) claims.get("role"))
                .build();

        } catch (Exception e) {
            return TokenValidationResponse.builder()
                .valid(false)
                .message("Token validation failed: " + e.getMessage())
                .build();
        }
    }
}
