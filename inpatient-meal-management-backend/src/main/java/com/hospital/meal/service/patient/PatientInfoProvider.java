package com.hospital.meal.service.patient;

import com.hospital.meal.dto.patient.PatientInfo;

public interface PatientInfoProvider {

    /**
     * Fetch patient info (name + room number) in a single call by UHID.
     * Returns null if patient is not found.
     */
    PatientInfo getPatientInfo(String uhid);

    /**
     * Check if patient exists in the system by UHID.
     */
    boolean patientExists(String uhid);
}