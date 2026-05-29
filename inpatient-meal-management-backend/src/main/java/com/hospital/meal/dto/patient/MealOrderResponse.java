package com.hospital.meal.dto.patient;

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
public class MealOrderResponse {

    private UUID orderId;
    private String uhid;
    private String patientName;
    private String roomNumber;
    private LocalDate orderDate;
    private List<MealItemDetail> meals;
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MealItemDetail {
        private UUID mealItemId;
        private String mealType;
        private String foodItemName;
        private String foodDescription;
        private String status;
        private LocalDateTime orderedAt;
        private LocalDateTime processedAt;
        private Boolean canModify; // Based on time window
    }
}