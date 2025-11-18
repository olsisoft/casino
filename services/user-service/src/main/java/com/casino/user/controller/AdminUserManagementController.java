package com.casino.user.controller;

import com.casino.user.entity.User;
import com.casino.user.service.AdminUserManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserManagementController {

    private final AdminUserManagementService adminUserManagementService;

    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        log.info("GET /admin/users?page={}&size={}", page, size);

        Page<User> users = adminUserManagementService.getAllUsers(PageRequest.of(page, size));

        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String query) {
        log.info("GET /admin/users/search?query={}", query);

        List<User> users = adminUserManagementService.searchUsers(query);

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserManagementService.UserDetailsDto> getUserDetails(
        @PathVariable String userId
    ) {
        log.info("GET /admin/users/{}", userId);

        AdminUserManagementService.UserDetailsDto details = adminUserManagementService.getUserDetails(userId);

        return ResponseEntity.ok(details);
    }

    @GetMapping("/{userId}/activity")
    public ResponseEntity<AdminUserManagementService.UserActivityStats> getUserActivity(
        @PathVariable String userId
    ) {
        log.info("GET /admin/users/{}/activity", userId);

        AdminUserManagementService.UserActivityStats activity = adminUserManagementService.getUserActivity(userId);

        return ResponseEntity.ok(activity);
    }

    @PostMapping("/{userId}/suspend")
    public ResponseEntity<User> suspendUser(
        @PathVariable String userId,
        @RequestHeader("X-Admin-Id") String adminId,
        @Valid @RequestBody SuspendRequest request
    ) {
        log.info("POST /admin/users/{}/suspend - adminId: {}", userId, adminId);

        User user = adminUserManagementService.suspendUser(userId, request.getReason(), adminId);

        return ResponseEntity.ok(user);
    }

    @PostMapping("/{userId}/activate")
    public ResponseEntity<User> activateUser(
        @PathVariable String userId,
        @RequestHeader("X-Admin-Id") String adminId
    ) {
        log.info("POST /admin/users/{}/activate - adminId: {}", userId, adminId);

        User user = adminUserManagementService.activateUser(userId, adminId);

        return ResponseEntity.ok(user);
    }

    @PostMapping("/{userId}/adjust-balance")
    public ResponseEntity<User> adjustBalance(
        @PathVariable String userId,
        @RequestHeader("X-Admin-Id") String adminId,
        @Valid @RequestBody AdjustBalanceRequest request
    ) {
        log.info("POST /admin/users/{}/adjust-balance - adminId: {}, amount: {}",
            userId, adminId, request.getAmount());

        User user = adminUserManagementService.adjustBalance(
            userId,
            request.getAmount(),
            request.getReason(),
            adminId
        );

        return ResponseEntity.ok(user);
    }

    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<Void> resetPassword(
        @PathVariable String userId,
        @RequestHeader("X-Admin-Id") String adminId,
        @Valid @RequestBody ResetPasswordRequest request
    ) {
        log.info("POST /admin/users/{}/reset-password - adminId: {}", userId, adminId);

        adminUserManagementService.resetUserPassword(userId, request.getNewPasswordHash(), adminId);

        return ResponseEntity.noContent().build();
    }

    @Data
    public static class SuspendRequest {
        @NotBlank
        private String reason;
    }

    @Data
    public static class AdjustBalanceRequest {
        private BigDecimal amount;
        @NotBlank
        private String reason;
    }

    @Data
    public static class ResetPasswordRequest {
        @NotBlank
        private String newPasswordHash;
    }
}
