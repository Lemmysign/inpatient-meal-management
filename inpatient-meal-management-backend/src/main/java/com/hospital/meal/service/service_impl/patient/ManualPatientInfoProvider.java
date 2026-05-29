package com.hospital.meal.service.service_impl.patient;

import com.hospital.meal.dto.patient.PatientInfo;
import com.hospital.meal.service.patient.PatientInfoProvider;
import lombok.extern.slf4j.Slf4j;

/**
 * Manual patient info provider.
 * Used when HIS integration is disabled.
 * Returns null to indicate patient must enter their own details.
 *
 * NOT annotated with @Service — registered via PatientInfoProviderConfig.
 */
@Slf4j
public class ManualPatientInfoProvider implements PatientInfoProvider {

    @Override
    public PatientInfo getPatientInfo(String uhid) {
        log.debug("Manual mode: Patient info will be provided by user for UHID: {}", uhid);
        // Return null — signals to the service layer that
        // name and room must come from user input
        return null;
    }

    @Override
    public boolean patientExists(String uhid) {
        log.debug("Manual mode: Cannot validate patient existence for UHID: {}", uhid);
        // Cannot validate in manual mode — assume patient exists
        return true;
    }
}