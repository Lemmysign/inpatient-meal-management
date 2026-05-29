package com.hospital.meal.dto.dietician;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMenuGroupRequest {

    private UUID id;

    @NotBlank(message = "Menu group name is required")
    private String name;

    private String description;

    private Boolean isActive;
}