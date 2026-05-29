package com.hospital.meal.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {

    private LocalDate date;
    private Long totalMealsOrdered;
    private Map<String, Long> ordersByMealType; // BREAKFAST: 100, LUNCH: 120, etc.
    private Map<String, Long> ordersByStatus; // PENDING: 50, PROCESSED: 150
    private Double averageProcessingTimeMinutes;
    private List<PeakHourData> peakOrderingHours;
    private List<DieticianStats> ordersPerDietician;
    private List<MenuGroupStats> mostUsedMenuGroups;
    private List<FoodItemStats> mostOrderedFoodItems;
    private List<OrderVolumeData> orderVolumeTrends;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PeakHourData {
        private Integer hour;
        private Long orderCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DieticianStats {
        private String dieticianName;
        private Long orderCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MenuGroupStats {
        private String menuGroupName;
        private Long usageCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FoodItemStats {
        private String foodItemName;
        private Long orderCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderVolumeData {
        private LocalDate date;
        private Long orderCount;
    }
}