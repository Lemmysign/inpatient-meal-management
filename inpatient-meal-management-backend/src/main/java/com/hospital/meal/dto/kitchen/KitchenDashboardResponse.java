package com.hospital.meal.dto.kitchen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitchenDashboardResponse {

    private LocalDate date;
    private Long totalOrdersToday;
    private Long pendingBreakfast;
    private Long pendingLunch;
    private Long pendingDinner;
    private Long processedMeals;
    private Double averageProcessingTimeMinutes;
    private Map<String, Long> mealsByStatus;
    private Map<String, Long> mealsByType;
}