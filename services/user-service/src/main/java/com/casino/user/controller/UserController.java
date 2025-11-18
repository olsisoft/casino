package com.casino.user.controller;

import com.casino.user.dto.*;
import com.casino.user.entity.UserSettings;
import com.casino.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<Void> createProfile(@RequestHeader("X-User-Id") String userId) {
        log.info("POST /users/create - userId: {}", userId);
        userService.createUserProfile(userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

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

    @PostMapping("/balance/update")
    public ResponseEntity<String> updateBalance(
        @RequestHeader("X-User-Id") String userId,
        @Valid @RequestBody BalanceUpdateRequest request
    ) {
        log.info("POST /users/balance/update - userId: {}, operation: {}", userId, request.getOperation());

        boolean success = false;
        if ("add".equals(request.getOperation())) {
            if ("virtual".equals(request.getType())) {
                userService.addVirtualBalance(userId, request.getAmount());
                success = true;
            } else if ("real".equals(request.getType())) {
                userService.addRealBalance(userId, request.getAmount());
                success = true;
            }
        } else if ("deduct".equals(request.getOperation())) {
            if ("virtual".equals(request.getType())) {
                success = userService.deductVirtualBalance(userId, request.getAmount());
            } else if ("real".equals(request.getType())) {
                success = userService.deductRealBalance(userId, request.getAmount());
            }
        }

        if (success) {
            return ResponseEntity.ok("Balance updated successfully");
        } else {
            return ResponseEntity.badRequest().body("Insufficient balance");
        }
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

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User Service is running");
    }
}
