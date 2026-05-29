package com.hospital.meal.service.patient;

import com.hospital.meal.model.user.Patient;

import java.util.Optional;
import java.util.UUID;

public interface PatientService {

    /**
     * Get or create patient by UHID, name, and room number
     */
    Patient getOrCreatePatient(String uhid, String name, String roomNumber);

    /**
     * Get patient by UHID
     */
    Optional<Patient> getPatientByUhid(String uhid);

    /**
     * Get patient by ID
     */
    Optional<Patient> getPatientById(UUID patientId);

    /**
     * Update patient information
     */
    Patient updatePatient(UUID patientId, String name, String roomNumber);

    /**
     * Check if patient has active menu
     */
    boolean hasActiveMenu(String uhid);
}