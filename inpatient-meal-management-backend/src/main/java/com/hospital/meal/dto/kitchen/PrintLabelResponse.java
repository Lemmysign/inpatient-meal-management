package com.hospital.meal.dto.kitchen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrintLabelResponse {

    private String uhid;
    private String patientName;
    private String roomNumber;
    private String mealType;
    private String foodItemName;
    private String status;
    private LocalDateTime processedAt;
    private String labelHtml; // HTML for 70x35mm label
}