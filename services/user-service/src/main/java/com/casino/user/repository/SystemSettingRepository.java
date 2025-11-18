package com.casino.user.repository;

import com.casino.user.entity.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {

    Optional<SystemSetting> findBySettingKey(String settingKey);

    List<SystemSetting> findByCategory(SystemSetting.Category category);

    List<SystemSetting> findByIsPublicTrue();

    boolean existsBySettingKey(String settingKey);
}
