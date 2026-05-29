package com.hospital.meal.mapper;

import com.hospital.meal.dto.dietician.FoodItemResponse;
import com.hospital.meal.model.menu.FoodItem;
import org.springframework.stereotype.Component;

@Component
public class FoodItemMapper {

    public FoodItemResponse toResponse(FoodItem foodItem) {
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
}