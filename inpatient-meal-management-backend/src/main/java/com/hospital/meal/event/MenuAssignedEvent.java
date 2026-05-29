package com.hospital.meal.event;

import com.hospital.meal.model.menu.PatientMenu;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class MenuAssignedEvent extends ApplicationEvent {

    private final PatientMenu patientMenu;

    public MenuAssignedEvent(Object source, PatientMenu patientMenu) {
        super(source);
        this.patientMenu = patientMenu;
    }
}