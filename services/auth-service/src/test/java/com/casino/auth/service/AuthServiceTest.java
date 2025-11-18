package com.casino.auth.service;

import com.casino.auth.dto.AuthResponse;
import com.casino.auth.dto.LoginRequest;
import com.casino.auth.dto.RegisterRequest;
import com.casino.auth.entity.User;
import com.casino.auth.exception.InvalidCredentialsException;
import com.casino.auth.exception.UserAlreadyExistsException;
import com.casino.auth.repository.RefreshTokenRepository;
import com.casino.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterNewUser() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
            .email("test@example.com")
            .username("testuser")
            .password("Password123!")
            .acceptTerms(true)
            .build();

        // When
        AuthResponse response = authService.register(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals("test@example.com", response.getUser().getEmail());
        assertEquals("testuser", response.getUser().getUsername());
        assertEquals("PLAYER", response.getUser().getRole());
        assertEquals(900L, response.getExpiresIn());

        // Verify user in database
        User savedUser = userRepository.findByEmail("test@example.com").orElse(null);
        assertNotNull(savedUser);
        assertEquals("testuser", savedUser.getUsername());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        RegisterRequest request1 = RegisterRequest.builder()
            .email("test@example.com")
            .username("testuser1")
            .password("Password123!")
            .acceptTerms(true)
            .build();
        authService.register(request1);

        // When & Then
        RegisterRequest request2 = RegisterRequest.builder()
            .email("test@example.com")
            .username("testuser2")
            .password("Password123!")
            .acceptTerms(true)
            .build();

        assertThrows(UserAlreadyExistsException.class, () -> {
            authService.register(request2);
        });
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        // Given
        RegisterRequest request1 = RegisterRequest.builder()
            .email("test1@example.com")
            .username("testuser")
            .password("Password123!")
            .acceptTerms(true)
            .build();
        authService.register(request1);

        // When & Then
        RegisterRequest request2 = RegisterRequest.builder()
            .email("test2@example.com")
            .username("testuser")
            .password("Password123!")
            .acceptTerms(true)
            .build();

        assertThrows(UserAlreadyExistsException.class, () -> {
            authService.register(request2);
        });
    }

    @Test
    void shouldLoginWithEmail() {
        // Given - Register user first
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("test@example.com")
            .username("testuser")
            .password("Password123!")
            .acceptTerms(true)
            .build();
        authService.register(registerRequest);

        // When - Login with email
        LoginRequest loginRequest = LoginRequest.builder()
            .identifier("test@example.com")
            .password("Password123!")
            .build();
        AuthResponse response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("test@example.com", response.getUser().getEmail());
    }

    @Test
    void shouldLoginWithUsername() {
        // Given - Register user first
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("test@example.com")
            .username("testuser")
            .password("Password123!")
            .acceptTerms(true)
            .build();
        authService.register(registerRequest);

        // When - Login with username
        LoginRequest loginRequest = LoginRequest.builder()
            .identifier("testuser")
            .password("Password123!")
            .build();
        AuthResponse response = authService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals("testuser", response.getUser().getUsername());
    }

    @Test
    void shouldThrowExceptionWhenLoginWithInvalidPassword() {
        // Given
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("test@example.com")
            .username("testuser")
            .password("Password123!")
            .acceptTerms(true)
            .build();
        authService.register(registerRequest);

        // When & Then
        LoginRequest loginRequest = LoginRequest.builder()
            .identifier("test@example.com")
            .password("WrongPassword!")
            .build();

        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(loginRequest);
        });
    }

    @Test
    void shouldRefreshToken() {
        // Given - Register and get initial tokens
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("test@example.com")
            .username("testuser")
            .password("Password123!")
            .acceptTerms(true)
            .build();
        AuthResponse initialResponse = authService.register(registerRequest);
        String refreshToken = initialResponse.getRefreshToken();

        // When - Refresh token
        AuthResponse newResponse = authService.refreshToken(refreshToken);

        // Then
        assertNotNull(newResponse);
        assertNotNull(newResponse.getAccessToken());
        assertNotNull(newResponse.getRefreshToken());
        assertNotEquals(initialResponse.getAccessToken(), newResponse.getAccessToken());
    }

    @Test
    void shouldLogoutUser() {
        // Given
        RegisterRequest registerRequest = RegisterRequest.builder()
            .email("test@example.com")
            .username("testuser")
            .password("Password123!")
            .acceptTerms(true)
            .build();
        AuthResponse response = authService.register(registerRequest);
        String userId = response.getUser().getId();

        // When
        authService.logout(userId);

        // Then - All tokens should be revoked
        long validTokens = refreshTokenRepository.countValidTokensByUserId(
            userId,
            java.time.LocalDateTime.now()
        );
        assertEquals(0, validTokens);
    }
}
