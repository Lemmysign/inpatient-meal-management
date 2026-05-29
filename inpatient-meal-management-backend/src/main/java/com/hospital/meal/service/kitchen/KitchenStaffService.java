package com.hospital.meal.service.kitchen;

import com.hospital.meal.model.user.KitchenStaff;

import java.util.Optional;
import java.util.UUID;

public interface KitchenStaffService {

    /**
     * Get kitchen staff by ID
     */
    Optional<KitchenStaff> getKitchenStaffById(UUID staffId);

    /**
     * Get kitchen staff by email
     */
    Optional<KitchenStaff> getKitchenStaffByEmail(String email);

    /**
     * Check if kitchen staff is active
     */
    boolean isActive(UUID staffId);

    /**
     * Update kitchen staff session duration
     */
    void updateSessionDuration(UUID staffId, Integer hours);

}