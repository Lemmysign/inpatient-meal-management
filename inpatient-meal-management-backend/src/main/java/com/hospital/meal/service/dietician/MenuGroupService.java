package com.hospital.meal.service.dietician;

import com.hospital.meal.dto.dietician.CreateMenuGroupRequest;
import com.hospital.meal.dto.dietician.MenuGroupResponse;
import com.hospital.meal.dto.dietician.UpdateMenuGroupRequest;
import com.hospital.meal.dto.common.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface MenuGroupService {

    /**
     * Create a new menu group
     */
    MenuGroupResponse createMenuGroup(CreateMenuGroupRequest request, UUID dieticianId);

    /**
     * Update existing menu group
     */
    MenuGroupResponse updateMenuGroup(UUID menuGroupId, UpdateMenuGroupRequest request, UUID dieticianId);

    /**
     * Delete (deactivate) menu group
     */
    void deleteMenuGroup(UUID menuGroupId, UUID dieticianId);

    /**
     * Get menu group by ID
     */
    MenuGroupResponse getMenuGroupById(UUID menuGroupId);

    /**
     * Get all active menu groups
     */
    List<MenuGroupResponse> getAllActiveMenuGroups();

    /**
     * Get all active menu groups (paginated)
     */
    PageResponse<MenuGroupResponse> getAllActiveMenuGroups(Pageable pageable);

    /**
     * Get predefined menu groups
     */
    List<MenuGroupResponse> getPredefinedMenuGroups();

    /**
     * Get menu groups created by dietician
     */
    List<MenuGroupResponse> getMenuGroupsByDietician(UUID dieticianId);

    /**
     * Search menu groups by name
     */
    PageResponse<MenuGroupResponse> searchMenuGroups(String search, Pageable pageable);
}