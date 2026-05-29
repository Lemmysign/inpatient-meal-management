package com.hospital.meal.mapper;

import com.hospital.meal.dto.kitchen.MealQueueItemResponse;
import com.hospital.meal.dto.kitchen.PrintLabelResponse;
import com.hospital.meal.model.order.MealOrderItem;
import org.springframework.stereotype.Component;

@Component
public class KitchenStaffMapper {

    public MealQueueItemResponse toMealQueueItemResponse(MealOrderItem item, Integer queuePosition) {
        if (item == null) return null;

        return MealQueueItemResponse.builder()
                .mealItemId(item.getId())
                .uhid(item.getMealOrder().getPatient().getUhid())
                .patientName(item.getMealOrder().getPatient().getName())
                .roomNumber(item.getMealOrder().getPatient().getRoomNumber())
                .mealType(item.getMealType())
                .foodItemName(item.getFoodItem().getName())
                .status(item.getMealStatus().getCode())
                .orderedAt(item.getOrderedAt())
                .processedAt(item.getProcessedAt())
                .processedByStaffName(item.getProcessedByStaff() != null ?
                        item.getProcessedByStaff().getName() : null)
                .queuePosition(queuePosition)
                .build();
    }

    public PrintLabelResponse toPrintLabelResponse(MealOrderItem item) {
        if (item == null) return null;

        return PrintLabelResponse.builder()
                .uhid(item.getMealOrder().getPatient().getUhid())
                .patientName(item.getMealOrder().getPatient().getName())
                .roomNumber(item.getMealOrder().getPatient().getRoomNumber())
                .mealType(item.getMealType())
                .foodItemName(item.getFoodItem().getName())
                .status(item.getMealStatus().getCode())
                .processedAt(item.getProcessedAt())
                .build();
    }
}