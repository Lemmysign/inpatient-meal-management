package com.hospital.meal.event;

import com.hospital.meal.model.order.MealOrderItem;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MealProcessedEvent extends ApplicationEvent {

    private final MealOrderItem mealOrderItem;

    public MealProcessedEvent(Object source, MealOrderItem mealOrderItem) {
        super(source);
        this.mealOrderItem = mealOrderItem;
    }
}