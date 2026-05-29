package com.hospital.meal.dto.patient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableMenuResponse {

    private String patientName;
    private String uhid;
    private String roomNumber;
    private List<MenuCategory> menuCategories;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MenuCategory {
        private UUID menuGroupId;
        private String menuGroupName;
        private String description;
        private List<FoodOption> breakfastOptions;
        private List<FoodOption> lunchOptions;
        private List<FoodOption> dinnerOptions;
        private List<FoodOption> extraOptions;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FoodOption {
        private UUID foodItemId;
        private String name;
        private String description;
        private String mealType;
    }
}