package com.hospital.meal.dto.patient;

import com.hospital.meal.validation.annotation.ValidMealSelection;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMealOrderRequest {

    @NotEmpty(message = "Meal selections cannot be empty")
    @Valid
    private List<MealSelectionRequest> meals;
}