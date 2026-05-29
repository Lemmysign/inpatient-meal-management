package com.hospital.meal.mapper;

import com.hospital.meal.dto.patient.PatientResponse;
import com.hospital.meal.model.user.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    public PatientResponse toResponse(Patient patient) {
        if (patient == null) return null;

        return PatientResponse.builder()
                .id(patient.getId())
                .uhid(patient.getUhid())
                .name(patient.getName())
                .roomNumber(patient.getRoomNumber())
                .isActive(patient.getIsActive())
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .build();
    }
}