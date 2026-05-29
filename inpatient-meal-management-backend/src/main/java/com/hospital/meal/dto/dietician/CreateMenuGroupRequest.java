package com.hospital.meal.dto.dietician;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMenuGroupRequest {

    @NotBlank(message = "Menu group name is required")
    private String name;

    private String description;
}