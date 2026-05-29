package com.hospital.meal.event;

import com.hospital.meal.model.order.MealOrder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MealOrderedEvent extends ApplicationEvent {

    private final MealOrder mealOrder;

    public MealOrderedEvent(Object source, MealOrder mealOrder) {
        super(source);
        this.mealOrder = mealOrder;
    }
}