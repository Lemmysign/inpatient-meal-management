package com.hospital.meal.mapper;

import com.hospital.meal.dto.patient.MealOrderResponse;
import com.hospital.meal.model.order.MealOrderItem;
import com.hospital.meal.util.MealTimeValidator;
import org.springframework.stereotype.Component;

@Component
public class MealOrderItemMapper {

    private final MealTimeValidator mealTimeValidator;

    public MealOrderItemMapper(MealTimeValidator mealTimeValidator) {
        this.mealTimeValidator = mealTimeValidator;
    }

    public MealOrderResponse.MealItemDetail toMealItemDetail(MealOrderItem item) {
        if (item == null) return null;

        // ✅ Only check canModify for regular meals (not EXTRA/à la carte)
        boolean canModify = false;
        if (!item.getMealType().equals("EXTRA")) {
            canModify = mealTimeValidator.canModifyMeal(item.getMealType());
        }

        return MealOrderResponse.MealItemDetail.builder()
                .mealItemId(item.getId())
                .mealType(item.getMealType())
                .foodItemName(item.getFoodItem().getName())
                .foodDescription(item.getFoodItem().getDescription())
                .status(item.getMealStatus().getCode())
                .orderedAt(item.getOrderedAt())
                .processedAt(item.getProcessedAt())
                .canModify(canModify)  // À la carte items: canModify = false
                .build();
    }
}