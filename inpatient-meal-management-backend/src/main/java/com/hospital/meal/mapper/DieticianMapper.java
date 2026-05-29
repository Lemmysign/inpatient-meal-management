package com.hospital.meal.mapper;

import com.hospital.meal.dto.dietician.FoodItemResponse;
import com.hospital.meal.dto.dietician.MenuGroupResponse;
import com.hospital.meal.dto.dietician.PatientMenuResponse;
import com.hospital.meal.model.menu.FoodItem;
import com.hospital.meal.model.menu.MenuGroup;
import com.hospital.meal.model.menu.PatientMenu;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DieticianMapper {

    public MenuGroupResponse toMenuGroupResponse(MenuGroup menuGroup) {
        if (menuGroup == null) return null;

        return MenuGroupResponse.builder()
                .id(menuGroup.getId())
                .name(menuGroup.getName())
                .description(menuGroup.getDescription())
                .isPredefined(menuGroup.getIsPredefined())
                .createdByDieticianName(menuGroup.getCreatedByDietician() != null ?
                        menuGroup.getCreatedByDietician().getName() : null)
                .createdByDieticianId(menuGroup.getCreatedByDietician() != null ?
                        menuGroup.getCreatedByDietician().getId() : null)
                .isActive(menuGroup.getIsActive())
                .createdAt(menuGroup.getCreatedAt())
                .updatedAt(menuGroup.getUpdatedAt())
                .build();
    }

    public FoodItemResponse toFoodItemResponse(FoodItem foodItem) {
        if (foodItem == null) return null;

        return FoodItemResponse.builder()
                .id(foodItem.getId())
                .name(foodItem.getName())
                .description(foodItem.getDescription())
                .mealType(foodItem.getMealType())
                .menuGroupId(foodItem.getMenuGroup().getId())
                .menuGroupName(foodItem.getMenuGroup().getName())
                .isActive(foodItem.getIsActive())
                .createdAt(foodItem.getCreatedAt())
                .updatedAt(foodItem.getUpdatedAt())
                .build();
    }

    public PatientMenuResponse toPatientMenuResponse(PatientMenu patientMenu) {
        if (patientMenu == null) return null;

        return PatientMenuResponse.builder()
                .id(patientMenu.getId())
                .patientId(patientMenu.getPatient().getId())
                .patientUhid(patientMenu.getPatient().getUhid())
                .patientName(patientMenu.getPatient().getName())
                .patientRoomNumber(patientMenu.getPatient().getRoomNumber())
                .menuGroupId(patientMenu.getMenuGroup().getId())
                .menuGroupName(patientMenu.getMenuGroup().getName())
                .menuGroupDescription(patientMenu.getMenuGroup().getDescription())
                .assignedByDieticianId(patientMenu.getAssignedByDietician().getId())
                .assignedByDieticianName(patientMenu.getAssignedByDietician().getName())
                .assignedAt(patientMenu.getAssignedAt())
                .validFrom(patientMenu.getValidFrom())
                .validUntil(patientMenu.getValidUntil())
                .isActive(patientMenu.getIsActive())
                .build();
    }

    public PatientMenuResponse toPatientMenuResponseWithFoodItems(PatientMenu patientMenu, List<FoodItem> foodItems) {
        PatientMenuResponse response = toPatientMenuResponse(patientMenu);
        if (response != null && foodItems != null) {
            response.setFoodItems(foodItems.stream()
                    .map(this::toFoodItemResponse)
                    .collect(Collectors.toList()));
        }
        return response;
    }
}