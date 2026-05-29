package com.hospital.meal.dto.dietician;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatientMenuResponse {

    private UUID id;
    private UUID patientId;
    private String patientUhid;
    private String patientName;
    private String patientRoomNumber;
    private UUID menuGroupId;
    private String menuGroupName;
    private String menuGroupDescription;
    private UUID assignedByDieticianId;
    private String assignedByDieticianName;
    private String notes;
    private LocalDateTime assignedAt;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private Boolean isActive;
    private List<FoodItemResponse> foodItems;
}