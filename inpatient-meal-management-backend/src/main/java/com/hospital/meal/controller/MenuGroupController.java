package com.hospital.meal.controller;

import com.hospital.meal.constant.ApiConstants;
import com.hospital.meal.dto.common.ApiResponse;
import com.hospital.meal.dto.common.PageResponse;
import com.hospital.meal.dto.dietician.CreateMenuGroupRequest;
import com.hospital.meal.dto.dietician.MenuGroupResponse;
import com.hospital.meal.dto.dietician.UpdateMenuGroupRequest;
import com.hospital.meal.service.dietician.MenuGroupService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.DIETICIAN_BASE + ApiConstants.DIETICIAN_MENU_GROUPS)
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Menu Group", description = "Menu group management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class MenuGroupController {

    private final MenuGroupService menuGroupService;

    /**
     * Create new menu group
     */
    @PostMapping
    @Operation(summary = "Create Menu Group", description = "Create a new custom menu group (dietician only)")
    public ResponseEntity<ApiResponse<MenuGroupResponse>> createMenuGroup(
            @Valid @RequestBody CreateMenuGroupRequest request,
            @RequestAttribute("userId") UUID dieticianId) {

        log.info("Create menu group request: {} by dietician: {}", request.getName(), dieticianId);

        MenuGroupResponse menuGroup = menuGroupService.createMenuGroup(request, dieticianId);

        log.info("Menu group created successfully: {}", menuGroup.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Menu group created successfully", menuGroup));
    }

    /**
     * Update menu group
     */
    @PutMapping("/{menuGroupId}")
    @Operation(summary = "Update Menu Group", description = "Update an existing menu group (creator only)")
    public ResponseEntity<ApiResponse<MenuGroupResponse>> updateMenuGroup(
            @PathVariable UUID menuGroupId,
            @Valid @RequestBody UpdateMenuGroupRequest request,
            @RequestAttribute("userId") UUID dieticianId) {

        log.info("Update menu group request: {} by dietician: {}", menuGroupId, dieticianId);

        MenuGroupResponse menuGroup = menuGroupService.updateMenuGroup(menuGroupId, request, dieticianId);

        log.info("Menu group updated successfully");

        return ResponseEntity.ok(
                ApiResponse.success("Menu group updated successfully", menuGroup)
        );
    }

    /**
     * Delete menu group
     */
    @DeleteMapping("/{menuGroupId}")
    @Operation(summary = "Delete Menu Group", description = "Delete a menu group (soft delete, creator only)")
    public ResponseEntity<ApiResponse<Void>> deleteMenuGroup(
            @PathVariable UUID menuGroupId,
            @RequestAttribute("userId") UUID dieticianId) {

        log.info("Delete menu group request: {} by dietician: {}", menuGroupId, dieticianId);

        menuGroupService.deleteMenuGroup(menuGroupId, dieticianId);

        log.info("Menu group deleted successfully");

        return ResponseEntity.ok(
                ApiResponse.success("Menu group deleted successfully", null)
        );
    }

    /**
     * Get menu group by ID
     */
    @GetMapping("/{menuGroupId}")
    @Operation(summary = "Get Menu Group", description = "Get menu group details by ID")
    public ResponseEntity<ApiResponse<MenuGroupResponse>> getMenuGroup(
            @PathVariable UUID menuGroupId) {

        log.info("Get menu group request: {}", menuGroupId);

        MenuGroupResponse menuGroup = menuGroupService.getMenuGroupById(menuGroupId);

        return ResponseEntity.ok(
                ApiResponse.success("Menu group retrieved successfully", menuGroup)
        );
    }

    /**
     * Get all active menu groups (non-paginated)
     */
    @GetMapping
    @Operation(summary = "Get All Menu Groups", description = "Get all active menu groups")
    public ResponseEntity<ApiResponse<List<MenuGroupResponse>>> getAllMenuGroups() {

        log.info("Get all menu groups request");

        List<MenuGroupResponse> menuGroups = menuGroupService.getAllActiveMenuGroups();

        return ResponseEntity.ok(
                ApiResponse.success("Menu groups retrieved successfully", menuGroups)
        );
    }

    /**
     * Get all active menu groups (paginated)
     */
    @GetMapping("/paged")
    @Operation(summary = "Get Menu Groups (Paginated)", description = "Get all active menu groups with pagination")
    public ResponseEntity<ApiResponse<PageResponse<MenuGroupResponse>>> getAllMenuGroupsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {

        log.info("Get all menu groups (paginated) request");

        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PageResponse<MenuGroupResponse> menuGroups = menuGroupService.getAllActiveMenuGroups(pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Menu groups retrieved successfully", menuGroups)
        );
    }

    /**
     * Get predefined menu groups
     */
    @GetMapping("/predefined")
    @Operation(summary = "Get Predefined Menu Groups", description = "Get system predefined menu groups (Diabetic, Stroke, etc.)")
    public ResponseEntity<ApiResponse<List<MenuGroupResponse>>> getPredefinedMenuGroups() {

        log.info("Get predefined menu groups request");

        List<MenuGroupResponse> menuGroups = menuGroupService.getPredefinedMenuGroups();

        return ResponseEntity.ok(
                ApiResponse.success("Predefined menu groups retrieved successfully", menuGroups)
        );
    }

    /**
     * Get menu groups created by specific dietician
     */
    @GetMapping("/my-menu-groups")
    @Operation(summary = "Get My Menu Groups", description = "Get menu groups created by the current dietician")
    public ResponseEntity<ApiResponse<List<MenuGroupResponse>>> getMyMenuGroups(
            @RequestAttribute("userId") UUID dieticianId) {

        log.info("Get menu groups for dietician: {}", dieticianId);

        List<MenuGroupResponse> menuGroups = menuGroupService.getMenuGroupsByDietician(dieticianId);

        return ResponseEntity.ok(
                ApiResponse.success("Menu groups retrieved successfully", menuGroups)
        );
    }

    /**
     * Search menu groups
     */
    @GetMapping("/search")
    @Operation(summary = "Search Menu Groups", description = "Search menu groups by name or description")
    public ResponseEntity<ApiResponse<PageResponse<MenuGroupResponse>>> searchMenuGroups(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Search menu groups request: {}", query);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));

        PageResponse<MenuGroupResponse> menuGroups = menuGroupService.searchMenuGroups(query, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Search results retrieved successfully", menuGroups)
        );
    }
}