package com.hospital.meal.service.service_impl.patient;

import com.hospital.meal.dto.patient.PatientInfo;
import com.hospital.meal.service.patient.PatientInfoProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * HIS patient info provider.
 * Fetches patient name and room number from the HIS system
 * by scraping the Patient Lists page.
 *
 * NOT annotated with @Service — registered via PatientInfoProviderConfig.
 */
@Slf4j
@RequiredArgsConstructor
public class HISPatientInfoProvider implements PatientInfoProvider {

    private final HISPatientScraper scraper;

    @Override
    public PatientInfo getPatientInfo(String uhid) {
        try {
            PatientInfo info = scraper.findPatientByUHID(uhid);
            if (info == null) {
                log.warn("Patient not found in HIS for UHID: {}", uhid);
            }
            return info;
        } catch (IOException e) {
            log.error("Failed to fetch patient info from HIS for UHID: {}. Error: {}",
                    uhid, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean patientExists(String uhid) {
        return getPatientInfo(uhid) != null;
    }
}