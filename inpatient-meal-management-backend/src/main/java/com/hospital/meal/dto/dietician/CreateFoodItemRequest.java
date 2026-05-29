package com.hospital.meal.dto.dietician;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFoodItemRequest {

    @NotNull(message = "Menu group ID is required")
    private UUID menuGroupId;

    @NotBlank(message = "Food item name is required")
    private String name;

    private String description;

    private String mealType; // BREAKFAST, LUNCH, DINNER, EXTRA (nullable = any meal)
}