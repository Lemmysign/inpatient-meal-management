package com.hospital.meal.dto.kitchen;

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
public class MealQueueItemResponse {

    private UUID mealItemId;
    private String uhid;
    private String patientName;
    private String roomNumber;
    private String mealType;
    private String foodItemName;
    private String status;
    private LocalDateTime orderedAt;
    private LocalDateTime processedAt;
    private String processedByStaffName;
    private Integer queuePosition;
    private String dieticianNotes; // Special dietary instructions
}