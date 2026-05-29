package com.hospital.meal.config;

import com.hospital.meal.model.config.HISIntegrationSetting;
import com.hospital.meal.repository.HISIntegrationSettingRepository;
import com.hospital.meal.service.patient.PatientInfoProvider;
import com.hospital.meal.service.service_impl.patient.HISPatientInfoProvider;
import com.hospital.meal.service.service_impl.patient.HISPatientScraper;
import com.hospital.meal.service.service_impl.patient.ManualPatientInfoProvider;
import com.hospital.meal.dto.patient.PatientInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures which PatientInfoProvider is active at runtime.
 *
 * Uses a delegating wrapper bean that checks the DB flag on every call.
 * This allows toggling between HIS and Manual mode without restarting the app.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PatientInfoProviderConfig {

    private final HISIntegrationSettingRepository settingRepository;
    private final HISPatientScraper hisScraper;

    @Bean
    public PatientInfoProvider patientInfoProvider() {

        PatientInfoProvider manualProvider = new ManualPatientInfoProvider();
        PatientInfoProvider hisProvider = new HISPatientInfoProvider(hisScraper);

        // Delegating wrapper — checks DB flag on every single call
        return new PatientInfoProvider() {

            @Override
            public PatientInfo getPatientInfo(String uhid) {
                if (isHISEnabled()) {
                    log.debug("HIS mode active — fetching patient from HIS for UHID: {}", uhid);
                    return hisProvider.getPatientInfo(uhid);
                }
                log.debug("Manual mode active — returning null for UHID: {}", uhid);
                return manualProvider.getPatientInfo(uhid);
            }

            @Override
            public boolean patientExists(String uhid) {
                if (isHISEnabled()) {
                    return hisProvider.patientExists(uhid);
                }
                return manualProvider.patientExists(uhid);
            }

            private boolean isHISEnabled() {
                return settingRepository.findById(1L)
                        .map(HISIntegrationSetting::isEnabled)
                        .orElse(false); // default to manual if no DB record exists
            }
        };
    }
}