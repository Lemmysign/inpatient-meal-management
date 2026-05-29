package com.hospital.meal.mapper;

import com.hospital.meal.dto.dietician.PatientMenuResponse;
import com.hospital.meal.model.menu.PatientMenu;
import org.springframework.stereotype.Component;

@Component
public class PatientMenuMapper {

    public PatientMenuResponse toResponse(PatientMenu patientMenu) {
        if (patientMenu == null) return null;

        return PatientMenuResponse.builder()
                .id(patientMenu.getId())
                .patientId(patientMenu.getPatient().getId())
                .patientUhid(patientMenu.getPatient().getUhid())
                .patientName(patientMenu.getPatient().getName())
                .patientRoomNumber(patientMenu.getPatient().getRoomNumber())
                .menuGroupId(patientMenu.getMenuGroup().getId())
                .menuGroupName(patientMenu.getMenuGroup().getName())
                .menuGroupDescription(patientMenu.getMenuGroup().getDescription())
                .assignedByDieticianId(patientMenu.getAssignedByDietician().getId())
                .assignedByDieticianName(patientMenu.getAssignedByDietician().getName())
                .assignedAt(patientMenu.getAssignedAt())
                .validFrom(patientMenu.getValidFrom())
                .validUntil(patientMenu.getValidUntil())
                .isActive(patientMenu.getIsActive())
                .notes(patientMenu.getNotes())  // ← ADD THIS
                .build();
    }
}