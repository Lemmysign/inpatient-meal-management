package com.hospital.meal.dto.dietician;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePatientMenuRequest {

    @NotNull(message = "Valid from date is required")
    private LocalDate validFrom;

    private LocalDate validUntil; // nullable = indefinite

    @NotNull(message = "Active status is required")
    private Boolean isActive;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    private UUID menuGroupId; // Optional: if provided, change the menu group
}