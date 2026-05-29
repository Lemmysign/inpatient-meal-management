package com.hospital.meal.dto.dietician;

import com.hospital.meal.validation.annotation.ValidUHID;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignPatientMenuRequest {

    @NotNull(message = "UHID is required")
    @ValidUHID
    private String uhid;

    @NotEmpty(message = "At least one menu group must be assigned")
    private List<UUID> menuGroupIds;

    @NotNull(message = "Valid from date is required")
    private LocalDate validFrom;

    private LocalDate validUntil; // nullable = indefinite

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes; // Optional: Special instructions for kitchen staff
}