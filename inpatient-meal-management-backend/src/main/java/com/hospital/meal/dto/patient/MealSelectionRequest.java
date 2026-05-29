package com.hospital.meal.dto.patient;

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
public class MealSelectionRequest {

    @NotNull(message = "Food item ID is required")
    private UUID foodItemId;

    @NotBlank(message = "Meal type is required")
    private String mealType; // BREAKFAST, LUNCH, DINNER, EXTRA
}