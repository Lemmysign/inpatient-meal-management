package com.hospital.meal.service.service_impl.patient;

import com.hospital.meal.exception.DuplicateResourceException;
import com.hospital.meal.exception.ResourceNotFoundException;
import com.hospital.meal.model.user.Patient;
import com.hospital.meal.repository.PatientMenuRepository;
import com.hospital.meal.repository.PatientRepository;
import com.hospital.meal.service.patient.PatientService;
import com.hospital.meal.util.DateUtil;
import com.hospital.meal.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientMenuRepository patientMenuRepository;

    @Override
    @Transactional
    public Patient getOrCreatePatient(String uhid, String name, String roomNumber) {
        log.info("Getting or creating patient with UHID: {}", uhid);

        // Validate inputs
        if (!ValidationUtil.isValidUHID(uhid)) {
            throw new IllegalArgumentException("Invalid UHID format");
        }

        if (ValidationUtil.isBlank(name)) {
            throw new IllegalArgumentException("Patient name cannot be blank");
        }

        if (!ValidationUtil.isValidRoomNumber(roomNumber)) {
            throw new IllegalArgumentException("Invalid room number format");
        }

        // Sanitize inputs
        String sanitizedUhid = ValidationUtil.sanitize(uhid);
        String sanitizedName = ValidationUtil.sanitize(name);
        String sanitizedRoomNumber = ValidationUtil.sanitize(roomNumber);

        // Check if patient exists
        Optional<Patient> existingPatient = patientRepository.findByUhid(sanitizedUhid);

        if (existingPatient.isPresent()) {
            Patient patient = existingPatient.get();

            log.info("Patient found with UHID: {}. Updating information.", sanitizedUhid);

            // Update name and room number if changed
            boolean updated = false;

            if (!Objects.equals(patient.getName(), sanitizedName)) {
                patient.setName(sanitizedName);
                updated = true;
            }

            // Update room number if changed (no availability check - multiple patients can share rooms)
            if (!Objects.equals(patient.getRoomNumber(), sanitizedRoomNumber)) {
                // ❌ COMMENTED OUT: Room availability check
                // Will be re-enabled when HIS API provides discharge status
                // checkRoomAvailability(sanitizedRoomNumber, patient.getId());

                patient.setRoomNumber(sanitizedRoomNumber);
                updated = true;
            }

            // Ensure patient is active
            if (!patient.getIsActive()) {
                patient.setIsActive(true);
                updated = true;
            }

            if (updated) {
                patient = patientRepository.save(patient);
                log.info("Patient information updated for UHID: {}", sanitizedUhid);
            }

            return patient;
        }

        // ❌ COMMENTED OUT: Room availability check for new patients
        // Will be re-enabled when HIS API provides discharge status
        // checkRoomAvailability(sanitizedRoomNumber, null);

        // Create new patient
        Patient newPatient = Patient.builder()
                .uhid(sanitizedUhid)
                .name(sanitizedName)
                .roomNumber(sanitizedRoomNumber)
                .isActive(true)
                .build();

        newPatient = patientRepository.save(newPatient);

        log.info("New patient created with UHID: {} and ID: {}", sanitizedUhid, newPatient.getId());

        return newPatient;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Patient> getPatientByUhid(String uhid) {
        log.debug("Getting patient by UHID: {}", uhid);

        if (!ValidationUtil.isValidUHID(uhid)) {
            log.warn("Invalid UHID format: {}", uhid);
            return Optional.empty();
        }

        return patientRepository.findActiveByUhid(ValidationUtil.sanitize(uhid));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Patient> getPatientById(UUID patientId) {
        log.debug("Getting patient by ID: {}", patientId);

        if (patientId == null) {
            log.warn("Patient ID is null");
            return Optional.empty();
        }

        return patientRepository.findById(patientId)
                .filter(Patient::getIsActive);
    }

    @Override
    @Transactional
    public Patient updatePatient(UUID patientId, String name, String roomNumber) {
        log.info("Updating patient with ID: {}", patientId);

        if (patientId == null) {
            throw new IllegalArgumentException("Patient ID cannot be null");
        }

        // Get patient
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patientId));

        // Validate and update name
        if (ValidationUtil.isNotBlank(name)) {
            String sanitizedName = ValidationUtil.sanitize(name);
            patient.setName(sanitizedName);
            log.debug("Updated patient name to: {}", sanitizedName);
        }

        // Validate and update room number (no availability check)
        if (ValidationUtil.isNotBlank(roomNumber)) {
            if (!ValidationUtil.isValidRoomNumber(roomNumber)) {
                throw new IllegalArgumentException("Invalid room number format");
            }
            String sanitizedRoomNumber = ValidationUtil.sanitize(roomNumber);

            // ❌ COMMENTED OUT: Room availability check
            // Will be re-enabled when HIS API provides discharge status
            // if (!patient.getRoomNumber().equals(sanitizedRoomNumber)) {
            //     checkRoomAvailability(sanitizedRoomNumber, patient.getId());
            // }

            patient.setRoomNumber(sanitizedRoomNumber);
            log.debug("Updated patient room number to: {}", sanitizedRoomNumber);
        }

        patient = patientRepository.save(patient);

        log.info("Patient updated successfully: {}", patientId);

        return patient;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveMenu(String uhid) {
        log.debug("Checking if patient has active menu: {}", uhid);

        if (!ValidationUtil.isValidUHID(uhid)) {
            log.warn("Invalid UHID format: {}", uhid);
            return false;
        }

        String sanitizedUhid = ValidationUtil.sanitize(uhid);

        boolean hasMenu = !patientMenuRepository
                .findActiveMenusByUhidAndDate(sanitizedUhid, DateUtil.getCurrentDate())
                .isEmpty();

        log.debug("Patient {} has active menu: {}", sanitizedUhid, hasMenu);

        return hasMenu;
    }

    // ========================================
    // ❌ COMMENTED OUT: Room availability check
    // ========================================
    // This method will be re-enabled when HIS API integration is complete
    // HIS API will provide discharge status as the source of truth for room occupancy
    //
    // /**
    //  * Check if a room number is available (not occupied by another active patient)
    //  *
    //  * @param roomNumber The room number to check
    //  * @param excludePatientId Patient ID to exclude from check (when updating existing patient)
    //  * @throws DuplicateResourceException if room is already occupied
    //  */
    // private void checkRoomAvailability(String roomNumber, UUID excludePatientId) {
    //     log.debug("Checking room availability for: {}", roomNumber);
    //
    //     List<Patient> patientsInRoom = patientRepository.findByRoomNumber(roomNumber);
    //
    //     // Filter out inactive patients and the patient being updated
    //     List<Patient> activePatientsInRoom = patientsInRoom.stream()
    //             .filter(Patient::getIsActive)
    //             .filter(p -> excludePatientId == null || !p.getId().equals(excludePatientId))
    //             .toList();
    //
    //     if (!activePatientsInRoom.isEmpty()) {
    //         Patient occupant = activePatientsInRoom.get(0);
    //         log.warn("Room {} is already occupied by patient with UHID: {}",
    //                 roomNumber, occupant.getUhid());
    //         throw new DuplicateResourceException(
    //                 String.format("Room %s is already occupied by patient %s (UHID: %s)",
    //                         roomNumber, occupant.getName(), occupant.getUhid())
    //         );
    //     }
    //
    //     log.debug("Room {} is available", roomNumber);
    // }
}