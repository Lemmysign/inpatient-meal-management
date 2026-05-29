package com.hospital.meal.dto.dietician;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMenuValidityRequest {

    @NotNull(message = "Valid from date is required")
    private LocalDate validFrom;

    private LocalDate validUntil; // Can be null for indefinite

    private Boolean isActive;  // ADD THIS

    private String notes;

    private UUID menuGroupId; // Optional: if provided, change the menu group// ADD THIS
}