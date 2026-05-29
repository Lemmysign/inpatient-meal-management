package com.hospital.meal.service.service_impl.dietician;

import com.hospital.meal.dto.common.PageResponse;
import com.hospital.meal.dto.dietician.CreateMenuGroupRequest;
import com.hospital.meal.dto.dietician.MenuGroupResponse;
import com.hospital.meal.dto.dietician.UpdateMenuGroupRequest;
import com.hospital.meal.exception.DuplicateResourceException;
import com.hospital.meal.exception.ResourceNotFoundException;
import com.hospital.meal.mapper.MenuGroupMapper;
import com.hospital.meal.model.menu.MenuGroup;
import com.hospital.meal.model.user.Dietician;
import com.hospital.meal.repository.DieticianRepository;
import com.hospital.meal.repository.FoodItemRepository;
import com.hospital.meal.repository.MenuGroupRepository;
import com.hospital.meal.repository.PatientMenuRepository;
import com.hospital.meal.service.dietician.MenuGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuGroupServiceImpl implements MenuGroupService {

    private final MenuGroupRepository menuGroupRepository;
    private final DieticianRepository dieticianRepository;
    private final FoodItemRepository foodItemRepository;
    private final PatientMenuRepository patientMenuRepository;
    private final MenuGroupMapper menuGroupMapper;

    @Override
    @Transactional
    public MenuGroupResponse createMenuGroup(CreateMenuGroupRequest request, UUID dieticianId) {
        log.info("Creating menu group: {} by dietician: {}", request.getName(), dieticianId);

        // Check for duplicate name
        if (menuGroupRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Menu Group", "name", request.getName());
        }

        // Get dietician
        Dietician dietician = dieticianRepository.findById(dieticianId)
                .orElseThrow(() -> new ResourceNotFoundException("Dietician", "id", dieticianId));

        // Create menu group
        MenuGroup menuGroup = MenuGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isPredefined(false) // Custom menu groups are not predefined
                .createdByDietician(dietician)
                .isActive(true)
                .build();

        menuGroup = menuGroupRepository.save(menuGroup);

        log.info("Menu group created successfully with ID: {}", menuGroup.getId());

        return toResponseWithCounts(menuGroup);
    }

    @Override
    @Transactional
    public MenuGroupResponse updateMenuGroup(UUID menuGroupId, UpdateMenuGroupRequest request, UUID dieticianId) {
        log.info("Updating menu group: {} by dietician: {}", menuGroupId, dieticianId);

        // Get menu group
        MenuGroup menuGroup = menuGroupRepository.findById(menuGroupId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu Group", "id", menuGroupId));

        // Check if predefined - only custom menu groups can be updated
        if (menuGroup.getIsPredefined()) {
            throw new IllegalStateException("Cannot modify predefined menu groups");
        }

        /* Check ownership - only creator can update
        if (!menuGroup.getCreatedByDietician().getId().equals(dieticianId)) {
            throw new IllegalStateException("Only the creator can modify this menu group");
        }
*/
        // Check for duplicate name (if name is changing)
        if (!menuGroup.getName().equals(request.getName())) {
            if (menuGroupRepository.existsByName(request.getName())) {
                throw new DuplicateResourceException("Menu Group", "name", request.getName());
            }
            menuGroup.setName(request.getName());
        }

        // Update fields
        if (request.getDescription() != null) {
            menuGroup.setDescription(request.getDescription());
        }

        if (request.getIsActive() != null) {
            menuGroup.setIsActive(request.getIsActive());
        }

        menuGroup = menuGroupRepository.save(menuGroup);

        log.info("Menu group updated successfully");

        return toResponseWithCounts(menuGroup);
    }

    @Override
    @Transactional
    public void deleteMenuGroup(UUID menuGroupId, UUID dieticianId) {
        log.info("Deleting menu group: {} by dietician: {}", menuGroupId, dieticianId);

        // Get menu group
        MenuGroup menuGroup = menuGroupRepository.findById(menuGroupId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu Group", "id", menuGroupId));

        // Check if predefined
        if (menuGroup.getIsPredefined()) {
            throw new IllegalStateException("Cannot delete predefined menu groups");
        }

        /* Check ownership
        if (!menuGroup.getCreatedByDietician().getId().equals(dieticianId)) {
            throw new IllegalStateException("Only the creator can delete this menu group");
        }
*/
        // Check if menu group is in use
        long assignmentCount = patientMenuRepository.findByMenuGroupId(menuGroupId).size();
        if (assignmentCount > 0) {
            throw new IllegalStateException(
                    "Cannot delete menu group - it is assigned to " + assignmentCount + " patient(s)");
        }

        // Soft delete - deactivate instead of hard delete
        menuGroup.setIsActive(false);
        menuGroupRepository.save(menuGroup);

        // Also deactivate associated food items
        foodItemRepository.findByMenuGroupId(menuGroupId).forEach(foodItem -> {
            foodItem.setIsActive(false);
            foodItemRepository.save(foodItem);
        });

        log.info("Menu group deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public MenuGroupResponse getMenuGroupById(UUID menuGroupId) {
        log.debug("Getting menu group by ID: {}", menuGroupId);

        MenuGroup menuGroup = menuGroupRepository.findById(menuGroupId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu Group", "id", menuGroupId));

        return toResponseWithCounts(menuGroup);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuGroupResponse> getAllActiveMenuGroups() {
        log.debug("Getting all active menu groups with patient counts");

        // Use the new query that includes patient count
        List<Object[]> results = menuGroupRepository.findAllWithPatientCount();

        return results.stream()
                .map(row -> {
                    MenuGroup menuGroup = (MenuGroup) row[0];
                    Long patientCount = (Long) row[1];
                    Long foodItemCount = foodItemRepository.countByMenuGroupId(menuGroup.getId());

                    return menuGroupMapper.toResponse(menuGroup, patientCount.intValue(), foodItemCount.intValue());
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MenuGroupResponse> getAllActiveMenuGroups(Pageable pageable) {
        log.debug("Getting all active menu groups (paginated)");

        Page<MenuGroup> menuGroupPage = menuGroupRepository.findAllActive(pageable);

        return PageResponse.<MenuGroupResponse>builder()
                .content(menuGroupPage.getContent().stream()
                        .map(this::toResponseWithCounts)
                        .collect(Collectors.toList()))
                .page(menuGroupPage.getNumber())
                .size(menuGroupPage.getSize())
                .totalElements(menuGroupPage.getTotalElements())
                .totalPages(menuGroupPage.getTotalPages())
                .first(menuGroupPage.isFirst())
                .last(menuGroupPage.isLast())
                .empty(menuGroupPage.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuGroupResponse> getPredefinedMenuGroups() {
        log.debug("Getting predefined menu groups");

        return menuGroupRepository.findPredefinedActive().stream()
                .map(this::toResponseWithCounts)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuGroupResponse> getMenuGroupsByDietician(UUID dieticianId) {
        log.debug("Getting menu groups created by dietician: {}", dieticianId);

        return menuGroupRepository.findByDieticianId(dieticianId).stream()
                .map(this::toResponseWithCounts)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MenuGroupResponse> searchMenuGroups(String search, Pageable pageable) {
        log.debug("Searching menu groups with query: {}", search);

        Page<MenuGroup> menuGroupPage = menuGroupRepository.searchMenuGroups(search, pageable);

        return PageResponse.<MenuGroupResponse>builder()
                .content(menuGroupPage.getContent().stream()
                        .map(this::toResponseWithCounts)
                        .collect(Collectors.toList()))
                .page(menuGroupPage.getNumber())
                .size(menuGroupPage.getSize())
                .totalElements(menuGroupPage.getTotalElements())
                .totalPages(menuGroupPage.getTotalPages())
                .first(menuGroupPage.isFirst())
                .last(menuGroupPage.isLast())
                .empty(menuGroupPage.isEmpty())
                .build();
    }

    /**
     * Helper method to convert MenuGroup to Response with food item count and patient count
     */
    private MenuGroupResponse toResponseWithCounts(MenuGroup menuGroup) {
        // Get food item count
        long foodItemCount = foodItemRepository.countByMenuGroupId(menuGroup.getId());

        // Get patient count (active assignments only)
        long patientCount = patientMenuRepository.countByMenuGroupIdAndIsActiveTrue(menuGroup.getId());

        return menuGroupMapper.toResponse(menuGroup, (int) patientCount, (int) foodItemCount);
    }
}