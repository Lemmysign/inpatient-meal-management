package com.hospital.meal.mapper;

import com.hospital.meal.dto.admin.DieticianResponse;
import com.hospital.meal.dto.admin.KitchenStaffResponse;
import com.hospital.meal.model.user.Dietician;
import com.hospital.meal.model.user.KitchenStaff;
import org.springframework.stereotype.Component;

@Component
public class AdminMapper {

    public DieticianResponse toDieticianResponse(Dietician dietician) {
        if (dietician == null) return null;

        return DieticianResponse.builder()
                .id(dietician.getId())
                .name(dietician.getName())
                .staffId(dietician.getStaffId())
                .email(dietician.getEmail())
                .phoneNumber(dietician.getPhoneNumber())
                .isActive(dietician.getIsActive())
                .hasPassword(dietician.getPasswordHash() != null)
                .createdAt(dietician.getCreatedAt())
                .updatedAt(dietician.getUpdatedAt())
                .build();
    }

    public KitchenStaffResponse toKitchenStaffResponse(KitchenStaff kitchenStaff) {
        if (kitchenStaff == null) return null;

        return KitchenStaffResponse.builder()
                .id(kitchenStaff.getId())
                .name(kitchenStaff.getName())
                .email(kitchenStaff.getEmail())
                .phoneNumber(kitchenStaff.getPhoneNumber())
                .isActive(kitchenStaff.getIsActive())
                .sessionDurationHours(kitchenStaff.getSessionDurationHours())
                .createdAt(kitchenStaff.getCreatedAt())
                .updatedAt(kitchenStaff.getUpdatedAt())
                .build();
    }
}