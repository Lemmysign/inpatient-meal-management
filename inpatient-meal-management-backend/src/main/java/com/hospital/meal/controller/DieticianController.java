package com.hospital.meal.controller;

import com.hospital.meal.constant.ApiConstants;
import com.hospital.meal.dto.common.ApiResponse;
import com.hospital.meal.dto.common.PageResponse;
import com.hospital.meal.dto.dietician.*;
import com.hospital.meal.exception.ResourceNotFoundException;
import com.hospital.meal.model.user.Dietician;
import com.hospital.meal.repository.DieticianRepository;
import com.hospital.meal.security.userdetails.UserPrincipal;
import com.hospital.meal.service.dietician.DieticianDashboardService;
import com.hospital.meal.service.dietician.DieticianService;
import com.hospital.meal.service.dietician.PatientMenuService;
import com.hospital.meal.util.DateUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.DIETICIAN_BASE)
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Dietician", description = "Dietician menu management and patient assignment endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DieticianController {

    private final PatientMenuService patientMenuService;
    private final DieticianDashboardService dieticianDashboardService;
    private final DieticianService dieticianService;


    /**
     * Get dietician dashboard metrics
     */
    @GetMapping(ApiConstants.DIETICIAN_DASHBOARD)
    @Operation(summary = "Get Dashboard", description = "Get dietician dashboard metrics and statistics")
    public ResponseEntity<ApiResponse<DieticianDashboardResponse>> getDashboard(
            @RequestParam(required = false) String date,
            @RequestAttribute("userId") UUID dieticianId) {

        log.info("Get dashboard request for dietician: {}", dieticianId);

        LocalDate targetDate = date != null ? LocalDate.parse(date) : DateUtil.getCurrentDate();

        DieticianDashboardResponse dashboard = dieticianDashboardService.getDashboardMetrics(dieticianId, targetDate);

        log.info("Dashboard metrics retrieved successfully");

        return ResponseEntity.ok(
                ApiResponse.success("Dashboard metrics retrieved successfully", dashboard)
        );
    }

    /**
     * Assign menu to patient
     */
    @PostMapping(ApiConstants.DIETICIAN_PATIENT_MENUS + "/assign")
    @Operation(summary = "Assign Menu", description = "Assign menu group(s) to a patient")
    public ResponseEntity<ApiResponse<List<PatientMenuResponse>>> assignMenu(
            @Valid @RequestBody AssignPatientMenuRequest request,
            @RequestAttribute("userId") UUID dieticianId) {

        log.info("Assign menu request for patient UHID: {} by dietician: {}",
                request.getUhid(), dieticianId);

        List<PatientMenuResponse> menus = patientMenuService.assignMenuToPatient(request, dieticianId);

        log.info("Menu assigned successfully to patient: {}", request.getUhid());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Menu assigned successfully", menus));
    }

    /**
     * Get menus assigned by this dietician
     */
    @GetMapping(ApiConstants.DIETICIAN_PATIENT_MENUS)
    @Operation(summary = "Get Assigned Menus", description = "Get all menus assigned by this dietician (paginated)")
    public ResponseEntity<ApiResponse<PageResponse<PatientMenuResponse>>> getAssignedMenus(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "assignedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestAttribute("userId") UUID dieticianId) {

        log.info("Get assigned menus request for dietician: {}", dieticianId);

        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PageResponse<PatientMenuResponse> menus = patientMenuService.getMenusByDietician(dieticianId, pageable);

        log.info("Assigned menus retrieved successfully");

        return ResponseEntity.ok(
                ApiResponse.success("Assigned menus retrieved successfully", menus)
        );
    }

    /**
     * Get active menus for a specific patient
     */
    @GetMapping("/patient/{uhid}/menus")
    @Operation(summary = "Get Patient Menus", description = "Get active menus for a specific patient")
    public ResponseEntity<ApiResponse<List<PatientMenuResponse>>> getPatientMenus(
            @PathVariable String uhid,
            @RequestParam(required = false) String date) {

        log.info("Get menus request for patient UHID: {}", uhid);

        LocalDate targetDate = date != null ? LocalDate.parse(date) : DateUtil.getCurrentDate();

        List<PatientMenuResponse> menus = patientMenuService.getActivePatientMenus(uhid, targetDate);

        log.info("Patient menus retrieved successfully");

        return ResponseEntity.ok(
                ApiResponse.success("Patient menus retrieved successfully", menus)
        );
    }

    /**
     * Get menu history for a patient
     */
    @GetMapping("/patient/{uhid}/menu-history")
    @Operation(summary = "Get Menu History", description = "Get all menu assignments for a patient (past and present)")
    public ResponseEntity<ApiResponse<List<PatientMenuResponse>>> getPatientMenuHistory(
            @PathVariable String uhid) {

        log.info("Get menu history request for patient UHID: {}", uhid);

        List<PatientMenuResponse> menus = patientMenuService.getPatientMenuHistory(uhid);

        log.info("Menu history retrieved successfully");

        return ResponseEntity.ok(
                ApiResponse.success("Menu history retrieved successfully", menus)
        );
    }

    /**
     * Update menu validity dates
     */
    @PutMapping("/patient-menus/{patientMenuId}")
    @Operation(summary = "Update Menu Validity", description = "Update the validity dates for a patient menu assignment")
    public ResponseEntity<ApiResponse<PatientMenuResponse>> updateMenuValidity(
            @PathVariable UUID patientMenuId,
            @Valid @RequestBody UpdateMenuValidityRequest request,
            @RequestAttribute("userId") UUID dieticianId) {

        log.info("Update menu validity request for patient menu: {}", patientMenuId);

        PatientMenuResponse menu = patientMenuService.updateMenuValidity(
                patientMenuId,
                request,  // CHANGED: Pass the entire request object
                dieticianId
        );

        log.info("Menu validity updated successfully");

        return ResponseEntity.ok(
                ApiResponse.success("Menu validity updated successfully", menu)
        );
    }
    /**
     * Deactivate patient menu
     */
    @DeleteMapping("/patient-menus/{patientMenuId}")
    @Operation(summary = "Deactivate Menu", description = "Deactivate a patient menu assignment")
    public ResponseEntity<ApiResponse<Void>> deactivateMenu(
            @PathVariable UUID patientMenuId,
            @RequestAttribute("userId") UUID dieticianId) {

        log.info("Deactivate menu request for patient menu: {}", patientMenuId);

        patientMenuService.deactivatePatientMenu(patientMenuId, dieticianId);

        log.info("Patient menu deactivated successfully");

        return ResponseEntity.ok(
                ApiResponse.success("Menu deactivated successfully", null)
        );
    }


    @GetMapping("/main-patient-menus")
    public ResponseEntity<ApiResponse<PageResponse<PatientMenuResponse>>> searchPatientMenus(
            @RequestParam(required = false) List<UUID> menuGroupIds,
            @RequestParam(required = false) List<UUID> dieticianIds,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "assignedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());

        PageResponse<PatientMenuResponse> response = patientMenuService.searchPatientMenus(
                menuGroupIds, dieticianIds, searchTerm, dateFrom, dateTo, isActive, pageable);

        return ResponseEntity.ok(ApiResponse.success("Patient menus retrieved successfully", response));
    }

    @PutMapping("/main-patient-menus/{patientMenuId}")
    public ResponseEntity<ApiResponse<PatientMenuResponse>> updatePatientMenuAssignment(
            @PathVariable UUID patientMenuId,
            @Valid @RequestBody UpdatePatientMenuRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        PatientMenuResponse response = patientMenuService.updatePatientMenuAssignment(
                patientMenuId, request, userPrincipal.getId());

        return ResponseEntity.ok(ApiResponse.success("Patient menu assignment updated successfully", response));
    }

    @DeleteMapping("/main-patient-menus/{patientMenuId}")
    public ResponseEntity<ApiResponse<Void>> removeMenuFromPatient(
            @PathVariable UUID patientMenuId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        patientMenuService.removeMenuFromPatient(patientMenuId, userPrincipal.getId());

        return ResponseEntity.ok(ApiResponse.success("Menu removed from patient successfully", null));
    }



    /**
     * Get total food items count
     */
    @GetMapping("/dashboard/total-food-items")
    @Operation(
            summary = "Get total food items count",
            description = "Returns total number of active food items"
    )
    public ResponseEntity<ApiResponse<Map<String, Long>>> getTotalFoodItems(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Getting total food items count");

        Long total = dieticianService.getTotalFoodItemsCount();

        Map<String, Long> response = new HashMap<>();
        response.put("totalFoodItems", total);

        return ResponseEntity.ok(
                ApiResponse.success("Total food items retrieved successfully", response)
        );
    }


    /**
     * Get all dieticians for filter dropdown
     */
    @GetMapping("/filters/dieticians")
    @Operation(
            summary = "Get dietician filter options",
            description = "Returns list of all active dieticians for use in filter dropdowns"
    )
    public ResponseEntity<ApiResponse<List<DieticianFilterOption>>> getDieticianFilterOptions(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Getting dietician filter options");

        List<DieticianFilterOption> options = dieticianService.getAllDieticianFilterOptions();

        return ResponseEntity.ok(
                ApiResponse.success("Dietician filter options retrieved successfully", options)
        );
    }

    /**
     * Get all menu groups for filter dropdown
     */
    @GetMapping("/filters/menu-groups")
    @Operation(
            summary = "Get menu group filter options",
            description = "Returns list of all active menu groups with patient counts for use in filter dropdowns"
    )
    public ResponseEntity<ApiResponse<List<MenuGroupFilterOption>>> getMenuGroupFilterOptions(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Getting menu group filter options");

        List<MenuGroupFilterOption> options = dieticianService.getAllMenuGroupFilterOptions();

        return ResponseEntity.ok(
                ApiResponse.success("Menu group filter options retrieved successfully", options)
        );
    }



}