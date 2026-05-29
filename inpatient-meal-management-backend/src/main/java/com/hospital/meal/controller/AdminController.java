package com.hospital.meal.controller;

import com.hospital.meal.constant.ApiConstants;
import com.hospital.meal.dto.admin.CreateDieticianRequest;
import com.hospital.meal.dto.admin.CreateKitchenStaffRequest;
import com.hospital.meal.dto.admin.DieticianResponse;
import com.hospital.meal.dto.admin.KitchenStaffResponse;
import com.hospital.meal.dto.common.ApiResponse;
import com.hospital.meal.dto.common.PageResponse;
import com.hospital.meal.security.userdetails.UserPrincipal;
import com.hospital.meal.service.admin.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.hospital.meal.model.config.HISIntegrationSetting;
import com.hospital.meal.repository.HISIntegrationSettingRepository;
import java.time.LocalDateTime;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.ADMIN_BASE)
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Admin", description = "Admin user management endpoints dev")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;
    private final HISIntegrationSettingRepository hisSettingRepository;

    // ========== DIETICIAN MANAGEMENT ==========

    /**
     * Create new dietician
     */
    @PostMapping(ApiConstants.ADMIN_DIETICIANS)
    @Operation(summary = "Create Dietician", description = "Create a new dietician account and send invitation email")
    public ResponseEntity<ApiResponse<DieticianResponse>> createDietician(
            @Valid @RequestBody CreateDieticianRequest request) {

        log.info("Create dietician request for email: {}", request.getEmail());

        DieticianResponse dietician = adminService.createDietician(request);

        log.info("Dietician created successfully: {}", dietician.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Dietician created successfully. Invitation email sent.", dietician));
    }

    /**
     * Get all dieticians (paginated)
     */
    @GetMapping(ApiConstants.ADMIN_DIETICIANS)
    @Operation(summary = "Get All Dieticians", description = "Get all dieticians with pagination")
    public ResponseEntity<ApiResponse<PageResponse<DieticianResponse>>> getAllDieticians(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {

        log.info("Get all dieticians request");

        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PageResponse<DieticianResponse> dieticians = adminService.getAllDieticians(pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Dieticians retrieved successfully", dieticians)
        );
    }

    /**
     * Get dietician by ID
     */
    @GetMapping(ApiConstants.ADMIN_DIETICIANS + "/{dieticianId}")
    @Operation(summary = "Get Dietician", description = "Get dietician details by ID")
    public ResponseEntity<ApiResponse<DieticianResponse>> getDietician(
            @PathVariable UUID dieticianId) {

        log.info("Get dietician request: {}", dieticianId);

        DieticianResponse dietician = adminService.getDieticianById(dieticianId);

        return ResponseEntity.ok(
                ApiResponse.success("Dietician retrieved successfully", dietician)
        );
    }

    /**
     * Toggle dietician active status
     */
    @PatchMapping(ApiConstants.ADMIN_DIETICIANS + "/{dieticianId}/status")
    @Operation(summary = "Toggle Dietician Status", description = "Enable or disable a dietician account")
    public ResponseEntity<ApiResponse<Void>> toggleDieticianStatus(
            @PathVariable UUID dieticianId,
            @RequestParam boolean isActive) {

        log.info("Toggle dietician status request: {} to {}", dieticianId, isActive);

        adminService.toggleDieticianStatus(dieticianId, isActive);

        log.info("Dietician status updated successfully");

        return ResponseEntity.ok(
                ApiResponse.success("Dietician status updated successfully", null)
        );
    }

    /**
     * Resend dietician invitation
     */
    @PostMapping(ApiConstants.ADMIN_DIETICIANS + "/{dieticianId}/resend-invite")
    @Operation(summary = "Resend Invitation", description = "Resend invitation email to a dietician")
    public ResponseEntity<ApiResponse<Void>> resendInvite(
            @PathVariable UUID dieticianId) {

        log.info("Resend invite request for dietician: {}", dieticianId);

        adminService.resendDieticianInvite(dieticianId);

        log.info("Invitation resent successfully");

        return ResponseEntity.ok(
                ApiResponse.success("Invitation email sent successfully", null)
        );
    }

    /**
     * Search dieticians
     */
    @GetMapping(ApiConstants.ADMIN_DIETICIANS + "/search")
    @Operation(summary = "Search Dieticians", description = "Search dieticians by name or email")
    public ResponseEntity<ApiResponse<PageResponse<DieticianResponse>>> searchDieticians(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Search dieticians request: {}", query);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));

        PageResponse<DieticianResponse> dieticians = adminService.searchDieticians(query, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Search results retrieved successfully", dieticians)
        );
    }

    // ========== KITCHEN STAFF MANAGEMENT ==========

    /**
     * Create new kitchen staff
     */
    @PostMapping(ApiConstants.ADMIN_KITCHEN_STAFF)
    @Operation(summary = "Create Kitchen Staff", description = "Create a new kitchen staff account")
    public ResponseEntity<ApiResponse<KitchenStaffResponse>> createKitchenStaff(
            @Valid @RequestBody CreateKitchenStaffRequest request) {

        log.info("Create kitchen staff request for email: {}", request.getEmail());

        KitchenStaffResponse staff = adminService.createKitchenStaff(request);

        log.info("Kitchen staff created successfully: {}", staff.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Kitchen staff created successfully", staff));
    }

    /**
     * Get all kitchen staff (paginated)
     */
    @GetMapping(ApiConstants.ADMIN_KITCHEN_STAFF)
    @Operation(summary = "Get All Kitchen Staff", description = "Get all kitchen staff with pagination")
    public ResponseEntity<ApiResponse<PageResponse<KitchenStaffResponse>>> getAllKitchenStaff(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {

        log.info("Get all kitchen staff request");

        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PageResponse<KitchenStaffResponse> staff = adminService.getAllKitchenStaff(pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Kitchen staff retrieved successfully", staff)
        );
    }

    /**
     * Get kitchen staff by ID
     */
    @GetMapping(ApiConstants.ADMIN_KITCHEN_STAFF + "/{staffId}")
    @Operation(summary = "Get Kitchen Staff", description = "Get kitchen staff details by ID")
    public ResponseEntity<ApiResponse<KitchenStaffResponse>> getKitchenStaff(
            @PathVariable UUID staffId) {

        log.info("Get kitchen staff request: {}", staffId);

        KitchenStaffResponse staff = adminService.getKitchenStaffById(staffId);

        return ResponseEntity.ok(
                ApiResponse.success("Kitchen staff retrieved successfully", staff)
        );
    }

    /**
     * Toggle kitchen staff active status
     */
    @PatchMapping(ApiConstants.ADMIN_KITCHEN_STAFF + "/{staffId}/status")
    @Operation(summary = "Toggle Kitchen Staff Status", description = "Enable or disable a kitchen staff account")
    public ResponseEntity<ApiResponse<Void>> toggleKitchenStaffStatus(
            @PathVariable UUID staffId,
            @RequestParam boolean isActive) {

        log.info("Toggle kitchen staff status request: {} to {}", staffId, isActive);

        adminService.toggleKitchenStaffStatus(staffId, isActive);

        log.info("Kitchen staff status updated successfully");

        return ResponseEntity.ok(
                ApiResponse.success("Kitchen staff status updated successfully", null)
        );
    }

    /**
     * Search kitchen staff
     */
    @GetMapping(ApiConstants.ADMIN_KITCHEN_STAFF + "/search")
    @Operation(summary = "Search Kitchen Staff", description = "Search kitchen staff by name or email")
    public ResponseEntity<ApiResponse<PageResponse<KitchenStaffResponse>>> searchKitchenStaff(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Search kitchen staff request: {}", query);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));

        PageResponse<KitchenStaffResponse> staff = adminService.searchKitchenStaff(query, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Search results retrieved successfully", staff)
        );
    }


    /**
     * Get current HIS integration status
     */
    @GetMapping("/his-integration/status")
    public ResponseEntity<ApiResponse<Boolean>> getHISStatus() {
        boolean enabled = hisSettingRepository.findById(1L)
                .map(HISIntegrationSetting::isEnabled)
                .orElse(false);
        return ResponseEntity.ok(ApiResponse.success(enabled));
    }

    /**
     * Toggle HIS integration on or off at runtime.
     * No app restart needed.
     */
    @PostMapping("/his-integration/toggle")
    public ResponseEntity<ApiResponse<String>> toggleHISIntegration(
            @RequestParam boolean enabled,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        HISIntegrationSetting setting = hisSettingRepository.findById(1L)
                .orElse(new HISIntegrationSetting());

        setting.setId(1L);
        setting.setEnabled(enabled);
        setting.setUpdatedBy(currentUser.getUsername());
        setting.setUpdatedAt(LocalDateTime.now());
        hisSettingRepository.save(setting);

        String status = enabled ? "HIS mode activated" : "Manual mode activated";
        log.info("HIS integration toggled to {} by {}", enabled, currentUser.getUsername());

        return ResponseEntity.ok(ApiResponse.success(status));
    }



}