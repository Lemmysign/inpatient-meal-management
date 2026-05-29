package com.hospital.meal.dto.dietician;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuGroupFilterOption {
    private UUID id;
    private String name;
    private String description;
    private Boolean isActive;
    private Integer assignedPatientsCount;
}