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

        // Create balance with 1000 virtual starting amount
        UserBalance balance = UserBalance.builder()
            .userId(userId)
            .build();
        balanceRepository.save(balance);

        // Create settings with defaults
        UserSettings settings = UserSettings.builder()
            .userId(userId)
            .build();
        settingsRepository.save(settings);

        log.info("User profile created successfully for: {}", userId);
    }

    public UserProfileDto getProfile(String userId) {
        log.info("Getting profile for user: {}", userId);
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
            .achievementsUnlocked(profile.getAchievementsUnlocked())
            .currentStreak(profile.getCurrentStreak())
            .bestStreak(profile.getBestStreak())
            .build();
    }

    @Transactional
    public UserProfileDto updateProfile(String userId, UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", userId);
        UserProfile profile = profileRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
        if (request.getLastName() != null) profile.setLastName(request.getLastName());
        if (request.getAvatarUrl() != null) profile.setAvatarUrl(request.getAvatarUrl());
        if (request.getDateOfBirth() != null) profile.setDateOfBirth(request.getDateOfBirth());
        if (request.getCountry() != null) profile.setCountry(request.getCountry());
        if (request.getPhoneNumber() != null) profile.setPhoneNumber(request.getPhoneNumber());

        profileRepository.save(profile);
        return getProfile(userId);
    }

    public BalanceDto getBalance(String userId) {
        log.info("Getting balance for user: {}", userId);
        UserBalance balance = balanceRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Balance not found"));

        return BalanceDto.builder()
            .virtualBalance(balance.getVirtualBalance())
            .realBalance(balance.getRealBalance())
            .bonusBalance(balance.getBonusBalance())
            .totalBalance(balance.getTotalBalance())
            .availableBalance(balance.getAvailableBalance())
            .lockedAmount(balance.getLockedAmount())
            .currency(balance.getCurrency())
            .build();
    }

    @Transactional
    public void addVirtualBalance(String userId, BigDecimal amount) {
        log.info("Adding {} to virtual balance for user: {}", amount, userId);
        balanceRepository.addVirtualBalance(userId, amount);
    }

    @Transactional
    public boolean deductVirtualBalance(String userId, BigDecimal amount) {
        log.info("Deducting {} from virtual balance for user: {}", amount, userId);
        int updated = balanceRepository.deductVirtualBalance(userId, amount);
        return updated > 0;
    }

    @Transactional
    public void addRealBalance(String userId, BigDecimal amount) {
        log.info("Adding {} to real balance for user: {}", amount, userId);
        balanceRepository.addRealBalance(userId, amount);
    }

    @Transactional
    public boolean deductRealBalance(String userId, BigDecimal amount) {
        log.info("Deducting {} from real balance for user: {}", amount, userId);
        int updated = balanceRepository.deductRealBalance(userId, amount);
        return updated > 0;
    }

    @Transactional
    public void lockAmount(String userId, BigDecimal amount) {
        log.info("Locking {} for user: {}", amount, userId);
        balanceRepository.lockAmount(userId, amount);
    }

    @Transactional
    public void unlockAmount(String userId, BigDecimal amount) {
        log.info("Unlocking {} for user: {}", amount, userId);
        balanceRepository.unlockAmount(userId, amount);
    }

    public UserSettings getSettings(String userId) {
        log.info("Getting settings for user: {}", userId);
        return settingsRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Settings not found"));
    }

    @Transactional
    public UserSettings updateSettings(String userId, UserSettings settings) {
        log.info("Updating settings for user: {}", userId);
        settings.setUserId(userId);
        return settingsRepository.save(settings);
    }

    @Transactional
    public void addXp(String userId, Long xp) {
        log.info("Adding {} XP to user: {}", xp, userId);
        UserProfile profile = profileRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Profile not found"));

        Long newXp = profile.getXp() + xp;
        Integer newLevel = calculateLevel(newXp);

        profileRepository.updateXpAndLevel(userId, newXp, newLevel);
    }

    @Transactional
    public void recordGamePlayed(String userId, Long wagered, Long won) {
        log.info("Recording game for user: {}, wagered: {}, won: {}", userId, wagered, won);
        profileRepository.incrementGamesPlayed(userId);
        profileRepository.addToTotalWagered(userId, wagered);
        profileRepository.addToTotalWon(userId, won);
    }

    private Integer calculateLevel(Long xp) {
        // Simple level calculation: level = sqrt(xp / 100)
        return (int) Math.floor(Math.sqrt(xp / 100.0)) + 1;
    }
}
