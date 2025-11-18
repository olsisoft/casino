# 🚀 Guide Complet d'Implémentation - Casino Platform

## ✅ Statut Actuel

**Auth Service**: 100% ✅ COMPLÉTÉ
**User Service**: 30% (entités créées)
**Game Service**: 0%
**Frontend**: 0%

## 📋 Ce qui reste à implémenter

### User Service (suite)

Les entités sont créées. Voici les repositories, services et controllers à créer:

#### 1. UserRepository.java
```java
package com.casino.user.repository;

import com.casino.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
}
```

#### 2. UserBalanceRepository.java
```java
package com.casino.user.repository;

import com.casino.user.entity.UserBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface UserBalanceRepository extends JpaRepository<UserBalance, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserBalance> findByUserId(String userId);

    @Modifying
    @Query("UPDATE UserBalance ub SET ub.virtualBalance = ub.virtualBalance + :amount WHERE ub.userId = :userId")
    void addVirtualBalance(@Param("userId") String userId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE UserBalance ub SET ub.virtualBalance = ub.virtualBalance - :amount WHERE ub.userId = :userId AND ub.virtualBalance >= :amount")
    int deductVirtualBalance(@Param("userId") String userId, @Param("amount") BigDecimal amount);
}
```

#### 3. UserSettingsRepository.java
```java
package com.casino.user.repository;

import com.casino.user.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, String> {
}
```

#### 4. UserService.java
```java
package com.casino.user.service;

import com.casino.user.dto.*;
import com.casino.user.entity.*;
import com.casino.user.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserProfileRepository profileRepository;
    private final UserBalanceRepository balanceRepository;
    private final UserSettingsRepository settingsRepository;

    @Transactional
    public void createUserProfile(String userId) {
        log.info("Creating profile for user: {}", userId);

        // Create profile
        UserProfile profile = UserProfile.builder()
            .userId(userId)
            .build();
        profileRepository.save(profile);

        // Create balance
        UserBalance balance = UserBalance.builder()
            .userId(userId)
            .build();
        balanceRepository.save(balance);

        // Create settings
        UserSettings settings = UserSettings.builder()
            .userId(userId)
            .build();
        settingsRepository.save(settings);

        log.info("User profile created successfully for: {}", userId);
    }

    public UserProfileDto getProfile(String userId) {
        UserProfile profile = profileRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Profile not found"));

        return UserProfileDto.builder()
            .userId(profile.getUserId())
            .firstName(profile.getFirstName())
            .lastName(profile.getLastName())
            .avatarUrl(profile.getAvatarUrl())
            .level(profile.getLevel())
            .xp(profile.getXp())
            .totalWagered(profile.getTotalWagered())
            .totalWon(profile.getTotalWon())
            .gamesPlayed(profile.getGamesPlayed())
            .build();
    }

    @Transactional
    public UserProfileDto updateProfile(String userId, UpdateProfileRequest request) {
        UserProfile profile = profileRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
        if (request.getLastName() != null) profile.setLastName(request.getLastName());
        if (request.getAvatarUrl() != null) profile.setAvatarUrl(request.getAvatarUrl());
        if (request.getDateOfBirth() != null) profile.setDateOfBirth(request.getDateOfBirth());
        if (request.getCountry() != null) profile.setCountry(request.getCountry());

        profileRepository.save(profile);

        return getProfile(userId);
    }

    public BalanceDto getBalance(String userId) {
        UserBalance balance = balanceRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Balance not found"));

        return BalanceDto.builder()
            .virtualBalance(balance.getVirtualBalance())
            .realBalance(balance.getRealBalance())
            .bonusBalance(balance.getBonusBalance())
            .totalBalance(balance.getTotalBalance())
            .availableBalance(balance.getAvailableBalance())
            .currency(balance.getCurrency())
            .build();
    }

    @Transactional
    public void addVirtualBalance(String userId, BigDecimal amount) {
        balanceRepository.addVirtualBalance(userId, amount);
    }

    @Transactional
    public boolean deductBalance(String userId, BigDecimal amount) {
        int updated = balanceRepository.deductVirtualBalance(userId, amount);
        return updated > 0;
    }

    public UserSettings getSettings(String userId) {
        return settingsRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Settings not found"));
    }

    @Transactional
    public UserSettings updateSettings(String userId, UserSettings settings) {
        settings.setUserId(userId);
        return settingsRepository.save(settings);
    }
}
```

#### 5. UserController.java
```java
package com.casino.user.controller;

import com.casino.user.dto.*;
import com.casino.user.entity.UserSettings;
import com.casino.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile(@RequestHeader("X-User-Id") String userId) {
        log.info("GET /users/profile - userId: {}", userId);
        UserProfileDto profile = userService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateProfile(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        log.info("PUT /users/profile - userId: {}", userId);
        UserProfileDto profile = userService.updateProfile(userId, request);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/balance")
    public ResponseEntity<BalanceDto> getBalance(@RequestHeader("X-User-Id") String userId) {
        log.info("GET /users/balance - userId: {}", userId);
        BalanceDto balance = userService.getBalance(userId);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/settings")
    public ResponseEntity<UserSettings> getSettings(@RequestHeader("X-User-Id") String userId) {
        log.info("GET /users/settings - userId: {}", userId);
        UserSettings settings = userService.getSettings(userId);
        return ResponseEntity.ok(settings);
    }

    @PutMapping("/settings")
    public ResponseEntity<UserSettings> updateSettings(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody UserSettings settings
    ) {
        log.info("PUT /users/settings - userId: {}", userId);
        UserSettings updated = userService.updateSettings(userId, settings);
        return ResponseEntity.ok(updated);
    }
}
```

#### 6. UserServiceApplication.java
```java
package com.casino.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

### DTOs manquants pour User Service

Créer dans `services/user-service/src/main/java/com/casino/user/dto/`:

**UserProfileDto.java**, **UpdateProfileRequest.java**, **BalanceDto.java**

### Service Discovery & API Gateway

#### Eureka Server Application
```java
package com.casino.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class ServiceDiscoveryApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceDiscoveryApplication.class, args);
    }
}
```

#### API Gateway Application
```java
package com.casino.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

## 🚀 Commandes pour Tout Démarrer

### 1. Build tous les services
```bash
# Auth Service
cd services/auth-service
mvn clean install

# User Service
cd ../user-service
mvn clean install

# Service Discovery
cd ../../infrastructure/service-discovery
mvn clean install

# API Gateway
cd ../api-gateway
mvn clean install
```

### 2. Démarrer l'infrastructure
```bash
# PostgreSQL et Redis
docker-compose up -d postgres redis

# Eureka
cd infrastructure/service-discovery
mvn spring-boot:run &

# Attendre 30 secondes
sleep 30
```

### 3. Démarrer les services
```bash
# Auth Service
cd services/auth-service
mvn spring-boot:run &

# User Service
cd ../user-service
mvn spring-boot:run &

# API Gateway
cd ../../infrastructure/api-gateway
mvn spring-boot:run &
```

### 4. Tester
```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "player1@test.com",
    "username": "player1",
    "password": "Password123!",
    "acceptTerms": true
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "player1@test.com",
    "password": "Password123!"
  }'

# Get Profile (utiliser le token reçu)
curl http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## 📝 Fichiers Créés Aujourd'hui

**Auth Service (100%)**:
- ✅ 22 fichiers Java
- ✅ Tests complets
- ✅ Configuration complète

**User Service (30%)**:
- ✅ 3 entités
- ⏸️ Repositories (guide fourni)
- ⏸️ Services (guide fourni)
- ⏸️ Controllers (guide fourni)

## 🎯 Priorités pour Finir Phase 1

1. **Terminer User Service** (2h)
   - Créer repositories
   - Créer services
   - Créer controllers
   - Créer DTOs

2. **Service Discovery & Gateway** (1h)
   - Créer main classes
   - Tester routing

3. **Game Service - Slots** (4h)
   - Entités de base
   - Slot logic
   - RNG service
   - Controller

4. **Frontend Basique** (8h)
   - Screens Auth
   - Navigation
   - API services
   - Slots UI basique

## 🔥 Bonne Nuit et Bon Développement!

Tout est prêt pour être implémenté. Suivez ce guide pour terminer rapidement.

**Total estimé Phase 1**: 15h restantes

Vous avez une excellente base ! 💪
