package com.hospital.meal.dto.kitchen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitchenQueueResponse {

    private String mealType;
    private String status;
    private Integer totalCount;
    private List<MealQueueItemResponse> items;
}