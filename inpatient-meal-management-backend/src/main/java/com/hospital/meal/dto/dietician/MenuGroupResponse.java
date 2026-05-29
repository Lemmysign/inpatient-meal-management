package com.hospital.meal.dto.dietician;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuGroupResponse {

    private UUID id;
    private String name;
    private String description;
    private Boolean isPredefined;
    private Boolean isAlacarte;
    private Integer assignedPatientsCount;
    private String createdByDieticianName;
    private UUID createdByDieticianId;
    private Boolean isActive;
    private Integer foodItemCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}