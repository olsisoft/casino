package com.casino.user.controller;

import com.casino.user.entity.SystemSetting;
import com.casino.user.service.SystemSettingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
public class SystemSettingController {

    private final SystemSettingService systemSettingService;

    @GetMapping
    public ResponseEntity<List<SystemSetting>> getAllSettings() {
        log.info("GET /admin/settings");

        List<SystemSetting> settings = systemSettingService.getAllSettings();

        return ResponseEntity.ok(settings);
    }

    @GetMapping("/{key}")
    public ResponseEntity<SystemSetting> getSetting(@PathVariable String key) {
        log.info("GET /admin/settings/{}", key);

        SystemSetting setting = systemSettingService.getSetting(key);

        return ResponseEntity.ok(setting);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<SystemSetting>> getSettingsByCategory(
        @PathVariable SystemSetting.Category category
    ) {
        log.info("GET /admin/settings/category/{}", category);

        List<SystemSetting> settings = systemSettingService.getSettingsByCategory(category);

        return ResponseEntity.ok(settings);
    }

    @GetMapping("/public")
    public ResponseEntity<Map<String, String>> getPublicSettings() {
        log.info("GET /admin/settings/public");

        Map<String, String> settings = systemSettingService.getPublicSettings();

        return ResponseEntity.ok(settings);
    }

    @PostMapping
    public ResponseEntity<SystemSetting> createOrUpdateSetting(
        @RequestHeader("X-Admin-Id") String adminId,
        @Valid @RequestBody UpsertSettingRequest request
    ) {
        log.info("POST /admin/settings - key: {} - adminId: {}", request.getKey(), adminId);

        SystemSetting setting = systemSettingService.upsertSetting(
            request.getKey(),
            request.getValue(),
            request.getCategory(),
            request.getDescription(),
            request.getIsPublic(),
            adminId
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(setting);
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteSetting(
        @PathVariable String key,
        @RequestHeader("X-Admin-Id") String adminId
    ) {
        log.info("DELETE /admin/settings/{} - adminId: {}", key, adminId);

        systemSettingService.deleteSetting(key, adminId);

        return ResponseEntity.noContent().build();
    }

    @Data
    public static class UpsertSettingRequest {
        @NotBlank
        private String key;
        @NotBlank
        private String value;
        private SystemSetting.Category category;
        private String description;
        private Boolean isPublic;
    }
}
