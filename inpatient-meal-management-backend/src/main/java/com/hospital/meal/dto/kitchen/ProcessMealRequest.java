package com.hospital.meal.dto.kitchen;

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
public class ProcessMealRequest {

    @NotNull(message = "Meal item ID is required")
    private UUID mealItemId;
}