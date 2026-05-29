package com.hospital.meal.service.admin;

import com.hospital.meal.dto.admin.CreateDieticianRequest;
import com.hospital.meal.dto.admin.CreateKitchenStaffRequest;
import com.hospital.meal.dto.admin.DieticianResponse;
import com.hospital.meal.dto.admin.KitchenStaffResponse;
import com.hospital.meal.dto.common.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AdminService {

    /**
     * Create a new dietician and send email invite
     */
    DieticianResponse createDietician(CreateDieticianRequest request);

    /**
     * Create a new kitchen staff member
     */
    KitchenStaffResponse createKitchenStaff(CreateKitchenStaffRequest request);

    /**
     * Get all dieticians (paginated)
     */
    PageResponse<DieticianResponse> getAllDieticians(Pageable pageable);

    /**
     * Get all kitchen staff (paginated)
     */
    PageResponse<KitchenStaffResponse> getAllKitchenStaff(Pageable pageable);

    /**
     * Get dietician by ID
     */
    DieticianResponse getDieticianById(UUID dieticianId);

    /**
     * Get kitchen staff by ID
     */
    KitchenStaffResponse getKitchenStaffById(UUID staffId);

    /**
     * Enable/disable dietician account
     */
    void toggleDieticianStatus(UUID dieticianId, boolean isActive);

    /**
     * Enable/disable kitchen staff account
     */
    void toggleKitchenStaffStatus(UUID staffId, boolean isActive);

    /**
     * Resend dietician invite
     */
    void resendDieticianInvite(UUID dieticianId);

    /**
     * Search dieticians by name or email
     */
    PageResponse<DieticianResponse> searchDieticians(String search, Pageable pageable);

    /**
     * Search kitchen staff by name or email
     */
    PageResponse<KitchenStaffResponse> searchKitchenStaff(String search, Pageable pageable);
}