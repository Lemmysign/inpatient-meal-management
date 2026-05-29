package com.hospital.meal.service.dietician;

import com.hospital.meal.dto.dietician.DieticianFilterOption;
import com.hospital.meal.dto.dietician.MenuGroupFilterOption;
import com.hospital.meal.model.user.Dietician;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DieticianService {

    /**
     * Get dietician by ID
     */
    Optional<Dietician> getDieticianById(UUID dieticianId);

    /**
     * Get dietician by email
     */
    Optional<Dietician> getDieticianByEmail(String email);

    /**
     * Get dietician by staff ID
     */
    Optional<Dietician> getDieticianByStaffId(String staffId);

    /**
     * Check if dietician is active
     */
    boolean isActive(UUID dieticianId);

    /**
     * Check if dietician has set password
     */
    boolean hasPassword(UUID dieticianId);

    /**
     * Update dietician profile
     */
    Dietician updateProfile(UUID dieticianId, String name, String phoneNumber);


    /**
     * Get all dieticians for filter dropdown
     */
    List<DieticianFilterOption> getAllDieticianFilterOptions();

    /**
     * Get all menu groups for filter dropdown
     */
    List<MenuGroupFilterOption> getAllMenuGroupFilterOptions();



    /**
     * Get total count of food items
     */
    Long getTotalFoodItemsCount();

}