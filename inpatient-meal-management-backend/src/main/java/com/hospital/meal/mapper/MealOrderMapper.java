package com.hospital.meal.mapper;

import com.hospital.meal.dto.patient.MealOrderResponse;
import com.hospital.meal.model.order.MealOrder;
import com.hospital.meal.model.order.MealOrderItem;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class MealOrderMapper {

    private final MealOrderItemMapper mealOrderItemMapper;

    public MealOrderMapper(MealOrderItemMapper mealOrderItemMapper) {
        this.mealOrderItemMapper = mealOrderItemMapper;
    }

    public MealOrderResponse toResponse(MealOrder mealOrder) {
        if (mealOrder == null) return null;

        return MealOrderResponse.builder()
                .orderId(mealOrder.getId())
                .uhid(mealOrder.getPatient().getUhid())
                .patientName(mealOrder.getPatient().getName())
                .roomNumber(mealOrder.getPatient().getRoomNumber())
                .orderDate(mealOrder.getOrderDate())
                .meals(mealOrder.getItems().stream()
                        .map(mealOrderItemMapper::toMealItemDetail)
                        .collect(Collectors.toList()))
                .createdAt(mealOrder.getCreatedAt())
                .build();
    }
}