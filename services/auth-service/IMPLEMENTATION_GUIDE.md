# Auth Service - Guide d'Implémentation

## ✅ Ce qui a été créé

### Entités JPA (Complété)
- ✅ `User.java` - Entité utilisateur avec roles, status, etc.
- ✅ `RefreshToken.java` - Tokens de rafraîchissement

### Repositories (Complété)
- ✅ `UserRepository.java` - Queries pour utilisateurs
- ✅ `RefreshTokenRepository.java` - Queries pour tokens

### DTOs (Complété)
- ✅ `RegisterRequest.java` - DTO pour inscription
- ✅ `LoginRequest.java` - DTO pour connexion
- ✅ `AuthResponse.java` - DTO de réponse
- ✅ `RefreshTokenRequest.java` - DTO pour refresh
- ✅ `TokenValidationResponse.java` - DTO validation

### Exceptions (Complété)
- ✅ `AuthException.java` - Exception de base
- ✅ `InvalidCredentialsException.java`
- ✅ `UserAlreadyExistsException.java`
- ✅ `InvalidTokenException.java`
- ✅ `GlobalExceptionHandler.java` - Gestionnaire global

### Configuration (Complété)
- ✅ `JwtProperties.java` - Configuration JWT

## 🔄 Services à Implémenter

### 1. JwtService.java

```java
package com.casino.auth.service;

import com.casino.auth.config.JwtProperties;
import com.casino.auth.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole().name());

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(user.getId())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration()))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
            .setSubject(user.getId())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshExpiration()))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject();
    }

    public Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
```

### 2. AuthService.java

```java
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
            // TODO: Validate 2FA code
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
```

### 3. SecurityConfig.java

```java
package com.casino.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
```

### 4. AuthController.java

```java
package com.casino.auth.controller;

import com.casino.auth.dto.*;
import com.casino.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /auth/register - email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /auth/login - identifier: {}", request.getIdentifier());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("POST /auth/refresh");
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        log.info("POST /auth/logout");
        // Extract userId from token
        String token = authHeader.replace("Bearer ", "");
        TokenValidationResponse validation = authService.validateToken(token);
        if (validation.isValid()) {
            authService.logout(validation.getUserId());
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(
        @RequestHeader("Authorization") String authHeader
    ) {
        log.info("POST /auth/validate");
        String token = authHeader.replace("Bearer ", "");
        TokenValidationResponse response = authService.validateToken(token);
        return ResponseEntity.ok(response);
    }
}
```

### 5. AuthServiceApplication.java (Main Class)

```java
package com.casino.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
```

## 🧪 Tests à Implémenter

### AuthServiceTest.java

```java
package com.casino.auth.service;

import com.casino.auth.dto.RegisterRequest;
import com.casino.auth.dto.AuthResponse;
import com.casino.auth.entity.User;
import com.casino.auth.exception.UserAlreadyExistsException;
import com.casino.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldRegisterNewUser() {
        RegisterRequest request = RegisterRequest.builder()
            .email("test@example.com")
            .username("testuser")
            .password("Password123!")
            .acceptTerms(true)
            .build();

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals("test@example.com", response.getUser().getEmail());
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        // Create user first
        RegisterRequest request1 = RegisterRequest.builder()
            .email("test@example.com")
            .username("testuser1")
            .password("Password123!")
            .acceptTerms(true)
            .build();
        authService.register(request1);

        // Try to register with same email
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
}
```

## 🚀 Pour Tester

### 1. Démarrer PostgreSQL et Redis
```bash
docker-compose up -d postgres redis
```

### 2. Build et lancer le service
```bash
cd services/auth-service
mvn clean install
mvn spring-boot:run
```

### 3. Tester avec curl

**Register:**
```bash
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "username": "player1",
    "password": "Password123!",
    "acceptTerms": true
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "user@example.com",
    "password": "Password123!"
  }'
```

## ✅ Checklist

- [ ] Créer JwtService.java
- [ ] Créer AuthService.java
- [ ] Créer SecurityConfig.java
- [ ] Créer AuthController.java
- [ ] Créer AuthServiceApplication.java
- [ ] Créer les tests
- [ ] Build le projet (mvn clean install)
- [ ] Tester le service

## 📝 Notes

- Le service écoute sur le port **8081**
- JWT expire après **15 minutes**
- Refresh token expire après **7 jours**
- Password doit contenir: majuscule, minuscule, chiffre
- Enregistre automatiquement avec Eureka

## 🔜 Prochaine Étape

Une fois l'Auth Service terminé et testé, passer au **User Service**.
