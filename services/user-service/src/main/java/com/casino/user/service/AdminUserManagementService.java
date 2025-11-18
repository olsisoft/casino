package com.casino.user.service;

import com.casino.user.entity.User;
import com.casino.user.exception.UserException;
import com.casino.user.repository.UserRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserManagementService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    /**
     * Get all users with pagination
     */
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Search users by criteria
     */
    public List<User> searchUsers(String searchTerm) {
        // Search by username, email, or ID
        List<User> byUsername = userRepository.findByUsernameContaining(searchTerm);
        if (!byUsername.isEmpty()) return byUsername;

        List<User> byEmail = userRepository.findByEmailContaining(searchTerm);
        if (!byEmail.isEmpty()) return byEmail;

        return userRepository.findById(searchTerm).map(List::of).orElse(List.of());
    }

    /**
     * Get user details
     */
    public UserDetailsDto getUserDetails(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException("User not found"));

        return UserDetailsDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .phoneNumber(user.getPhoneNumber())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .dateOfBirth(user.getDateOfBirth())
            .country(user.getCountry())
            .isActive(user.getIsActive())
            .isEmailVerified(user.getIsEmailVerified())
            .isPhoneVerified(user.getIsPhoneVerified())
            .balance(user.getBalance())
            .currency(user.getCurrency())
            .lastLoginAt(user.getLastLoginAt())
            .createdAt(user.getCreatedAt())
            .build();
    }

    /**
     * Suspend user account
     */
    @Transactional
    public User suspendUser(String userId, String reason, String adminId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException("User not found"));

        user.setIsActive(false);
        User updated = userRepository.save(user);

        auditLogService.log(
            adminId,
            com.casino.user.entity.AuditLog.ActionType.SUSPEND,
            "User",
            userId,
            "User suspended: " + reason,
            null,
            null,
            com.casino.user.entity.AuditLog.Severity.WARNING
        );

        log.info("User {} suspended by admin {}: {}", userId, adminId, reason);

        return updated;
    }

    /**
     * Activate user account
     */
    @Transactional
    public User activateUser(String userId, String adminId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException("User not found"));

        user.setIsActive(true);
        User updated = userRepository.save(user);

        auditLogService.log(
            adminId,
            com.casino.user.entity.AuditLog.ActionType.ACTIVATE,
            "User",
            userId,
            "User activated",
            null,
            null,
            com.casino.user.entity.AuditLog.Severity.INFO
        );

        log.info("User {} activated by admin {}", userId, adminId);

        return updated;
    }

    /**
     * Adjust user balance (admin override)
     */
    @Transactional
    public User adjustBalance(String userId, BigDecimal amount, String reason, String adminId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException("User not found"));

        BigDecimal oldBalance = user.getBalance();
        user.setBalance(user.getBalance().add(amount));
        User updated = userRepository.save(user);

        auditLogService.log(
            adminId,
            com.casino.user.entity.AuditLog.ActionType.ADJUST_BALANCE,
            "User",
            userId,
            String.format("Balance adjusted: %s. Reason: %s", amount, reason),
            oldBalance.toString(),
            updated.getBalance().toString(),
            com.casino.user.entity.AuditLog.Severity.WARNING
        );

        log.warn("Balance adjusted for user {}: {} by admin {}. Reason: {}",
            userId, amount, adminId, reason);

        return updated;
    }

    /**
     * Reset user password (admin)
     */
    @Transactional
    public void resetUserPassword(String userId, String newPasswordHash, String adminId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException("User not found"));

        user.setPasswordHash(newPasswordHash);
        userRepository.save(user);

        auditLogService.log(
            adminId,
            com.casino.user.entity.AuditLog.ActionType.UPDATE,
            "User",
            userId,
            "Password reset by admin",
            null,
            null,
            com.casino.user.entity.AuditLog.Severity.WARNING
        );

        log.info("Password reset for user {} by admin {}", userId, adminId);
    }

    /**
     * Get user activity statistics
     */
    public UserActivityStats getUserActivity(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserException("User not found"));

        // In production, this would query game/transaction history
        return UserActivityStats.builder()
            .userId(userId)
            .totalGamesPlayed(0L)
            .totalWagered(BigDecimal.ZERO)
            .totalWon(BigDecimal.ZERO)
            .totalDeposits(BigDecimal.ZERO)
            .totalWithdrawals(BigDecimal.ZERO)
            .lastActivity(user.getLastLoginAt())
            .accountAge(java.time.Period.between(
                user.getCreatedAt().toLocalDate(),
                LocalDateTime.now().toLocalDate()
            ).getDays())
            .build();
    }

    @Data
    @Builder
    public static class UserDetailsDto {
        private String id;
        private String username;
        private String email;
        private String phoneNumber;
        private String firstName;
        private String lastName;
        private java.time.LocalDate dateOfBirth;
        private String country;
        private Boolean isActive;
        private Boolean isEmailVerified;
        private Boolean isPhoneVerified;
        private BigDecimal balance;
        private String currency;
        private LocalDateTime lastLoginAt;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    public static class UserActivityStats {
        private String userId;
        private Long totalGamesPlayed;
        private BigDecimal totalWagered;
        private BigDecimal totalWon;
        private BigDecimal totalDeposits;
        private BigDecimal totalWithdrawals;
        private LocalDateTime lastActivity;
        private Integer accountAge;
    }
}
