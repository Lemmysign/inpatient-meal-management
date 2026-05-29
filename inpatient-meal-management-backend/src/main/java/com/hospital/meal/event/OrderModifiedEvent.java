package com.hospital.meal.event;

import com.hospital.meal.model.order.OrderModificationLog;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderModifiedEvent extends ApplicationEvent {

    private final OrderModificationLog modificationLog;

    public OrderModifiedEvent(Object source, OrderModificationLog modificationLog) {
        super(source);
        this.modificationLog = modificationLog;
    }
}