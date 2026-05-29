package com.hospital.meal.service.dietician;

import com.hospital.meal.dto.dietician.AssignPatientMenuRequest;
import com.hospital.meal.dto.dietician.PatientMenuResponse;
import com.hospital.meal.dto.common.PageResponse;
import com.hospital.meal.dto.dietician.UpdateMenuValidityRequest;
import com.hospital.meal.dto.dietician.UpdatePatientMenuRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PatientMenuService {

    /**
     * Assign menu groups to a patient
     */
    List<PatientMenuResponse> assignMenuToPatient(AssignPatientMenuRequest request, UUID dieticianId);

    /**
     * Get active menus for a patient on a specific date
     */
    List<PatientMenuResponse> getActivePatientMenus(String uhid, LocalDate date);

    /**
     * Get all menus assigned to a patient
     */
    List<PatientMenuResponse> getPatientMenuHistory(String uhid);

    /**
     * Get all patient menus assigned by a dietician
     */
    PageResponse<PatientMenuResponse> getMenusByDietician(UUID dieticianId, Pageable pageable);

    /**
     * Deactivate patient menu
     */
    void deactivatePatientMenu(UUID patientMenuId, UUID dieticianId);

    /**
     * Update patient menu validity dates
     */
    PatientMenuResponse updateMenuValidity(UUID patientMenuId, UpdateMenuValidityRequest request, UUID dieticianId);
    /**
     * Search patient menus with filters (all dieticians)
     */
    PageResponse<PatientMenuResponse> searchPatientMenus(
            List<UUID> menuGroupIds,
            List<UUID> dieticianIds,
            String searchTerm,
            LocalDate dateFrom,
            LocalDate dateTo,
            Boolean isActive,
            Pageable pageable);

    /**
     * Update patient menu assignment
     */
    PatientMenuResponse updatePatientMenuAssignment(
            UUID patientMenuId,
            UpdatePatientMenuRequest request,
            UUID dieticianId);

    /**
     * Remove menu from patient (deactivate)
     */
    void removeMenuFromPatient(UUID patientMenuId, UUID dieticianId);

}