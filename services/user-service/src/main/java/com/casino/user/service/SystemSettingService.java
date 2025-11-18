package com.casino.user.service;

import com.casino.user.entity.SystemSetting;
import com.casino.user.exception.UserException;
import com.casino.user.repository.SystemSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSettingService {

    private final SystemSettingRepository systemSettingRepository;
    private final AuditLogService auditLogService;

    /**
     * Get setting by key
     */
    public SystemSetting getSetting(String key) {
        return systemSettingRepository.findBySettingKey(key)
            .orElseThrow(() -> new UserException("Setting not found: " + key));
    }

    /**
     * Get setting value
     */
    public String getSettingValue(String key) {
        return systemSettingRepository.findBySettingKey(key)
            .map(SystemSetting::getSettingValue)
            .orElse(null);
    }

    /**
     * Get all settings
     */
    public List<SystemSetting> getAllSettings() {
        return systemSettingRepository.findAll();
    }

    /**
     * Get settings by category
     */
    public List<SystemSetting> getSettingsByCategory(SystemSetting.Category category) {
        return systemSettingRepository.findByCategory(category);
    }

    /**
     * Get public settings (for client access)
     */
    public Map<String, String> getPublicSettings() {
        return systemSettingRepository.findByIsPublicTrue().stream()
            .collect(Collectors.toMap(
                SystemSetting::getSettingKey,
                SystemSetting::getSettingValue
            ));
    }

    /**
     * Create or update setting
     */
    @Transactional
    public SystemSetting upsertSetting(String key,
                                      String value,
                                      SystemSetting.Category category,
                                      String description,
                                      Boolean isPublic,
                                      String adminId) {

        SystemSetting setting = systemSettingRepository.findBySettingKey(key)
            .orElse(SystemSetting.builder()
                .settingKey(key)
                .category(category)
                .valueType(SystemSetting.ValueType.STRING)
                .isEditable(true)
                .build());

        String oldValue = setting.getSettingValue();
        setting.setSettingValue(value);
        setting.setDescription(description);
        setting.setIsPublic(isPublic != null ? isPublic : false);
        setting.setUpdatedBy(adminId);

        SystemSetting saved = systemSettingRepository.save(setting);

        auditLogService.log(
            adminId,
            com.casino.user.entity.AuditLog.ActionType.SYSTEM_CONFIG_CHANGE,
            "SystemSetting",
            saved.getId(),
            "Setting updated: " + key,
            oldValue,
            value,
            com.casino.user.entity.AuditLog.Severity.WARNING
        );

        log.info("Setting {} updated by admin {}: {} -> {}", key, adminId, oldValue, value);

        return saved;
    }

    /**
     * Delete setting
     */
    @Transactional
    public void deleteSetting(String key, String adminId) {
        SystemSetting setting = getSetting(key);

        if (!setting.getIsEditable()) {
            throw new UserException("Setting is not editable: " + key);
        }

        systemSettingRepository.delete(setting);

        auditLogService.log(
            adminId,
            com.casino.user.entity.AuditLog.ActionType.DELETE,
            "SystemSetting",
            setting.getId(),
            "Setting deleted: " + key,
            setting.getSettingValue(),
            null,
            com.casino.user.entity.AuditLog.Severity.WARNING
        );

        log.info("Setting {} deleted by admin {}", key, adminId);
    }
}
