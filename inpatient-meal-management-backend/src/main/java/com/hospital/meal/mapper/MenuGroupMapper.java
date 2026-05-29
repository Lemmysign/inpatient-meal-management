// MenuGroupMapper.java - Complete updated version

package com.hospital.meal.mapper;

import com.hospital.meal.dto.dietician.CreateMenuGroupRequest;
import com.hospital.meal.dto.dietician.MenuGroupResponse;
import com.hospital.meal.dto.dietician.UpdateMenuGroupRequest;
import com.hospital.meal.model.menu.MenuGroup;
import org.springframework.stereotype.Component;

@Component
public class MenuGroupMapper {

    /**
     * Basic mapper - used when counts are not available
     */
    public MenuGroupResponse toResponse(MenuGroup menuGroup) {
        return MenuGroupResponse.builder()
                .id(menuGroup.getId())
                .name(menuGroup.getName())
                .description(menuGroup.getDescription())
                .isPredefined(menuGroup.getIsPredefined())
                .createdByDieticianName(
                        menuGroup.getCreatedByDietician() != null
                                ? menuGroup.getCreatedByDietician().getName()
                                : null
                )
                .createdByDieticianId(
                        menuGroup.getCreatedByDietician() != null
                                ? menuGroup.getCreatedByDietician().getId()
                                : null
                )
                .isActive(menuGroup.getIsActive())
                .foodItemCount(0) // Default
                .assignedPatientsCount(0) // Default
                .createdAt(menuGroup.getCreatedAt())
                .updatedAt(menuGroup.getUpdatedAt())
                .build();
    }

    /**
     * Mapper with patient count and food item count
     * This is the main method used by the service
     */
    public MenuGroupResponse toResponse(MenuGroup menuGroup, Integer assignedPatientsCount, Integer foodItemCount) {
        return MenuGroupResponse.builder()
                .id(menuGroup.getId())
                .name(menuGroup.getName())
                .description(menuGroup.getDescription())
                .isPredefined(menuGroup.getIsPredefined())
                .createdByDieticianName(
                        menuGroup.getCreatedByDietician() != null
                                ? menuGroup.getCreatedByDietician().getName()
                                : null
                )
                .createdByDieticianId(
                        menuGroup.getCreatedByDietician() != null
                                ? menuGroup.getCreatedByDietician().getId()
                                : null
                )
                .isActive(menuGroup.getIsActive())
                .foodItemCount(foodItemCount)
                .assignedPatientsCount(assignedPatientsCount)
                .createdAt(menuGroup.getCreatedAt())
                .updatedAt(menuGroup.getUpdatedAt())
                .build();
    }

    public MenuGroup toEntity(CreateMenuGroupRequest request) {
        return MenuGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isPredefined(false)
                .isActive(true)
                .build();
    }

    public void updateEntity(MenuGroup menuGroup, UpdateMenuGroupRequest request) {
        if (request.getName() != null) {
            menuGroup.setName(request.getName());
        }
        if (request.getDescription() != null) {
            menuGroup.setDescription(request.getDescription());
        }
        if (request.getIsActive() != null) {
            menuGroup.setIsActive(request.getIsActive());
        }
    }
}