package com.hospital.meal.repository;

import com.hospital.meal.model.config.HISIntegrationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HISIntegrationSettingRepository extends JpaRepository<HISIntegrationSetting, Long> {
}