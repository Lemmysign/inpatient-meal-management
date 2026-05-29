package com.hospital.meal.service.patient;

import com.hospital.meal.dto.patient.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MealOrderService {

    /**
     * Get available menu for patient
     */
    AvailableMenuResponse getAvailableMenu(String uhid, LocalDate date);

    /**
     * Create meal order for the current day
     */
    MealOrderResponse createMealOrder(String uhid, CreateMealOrderRequest request);

    /**
     * Get today's meal order for patient
     */
    MealOrderResponse getTodaysMealOrder(String uhid);

    /**
     * Get meal order history for patient
     */
    List<MealOrderResponse> getMealOrderHistory(String uhid);

    /**
     * Modify specific meal in today's order (if within cutoff time)
     */
    MealOrderResponse modifyMeal(String uhid, String mealType, UUID newFoodItemId, String reason);

    /**
     * Check if meal can be modified
     */
    boolean canModifyMeal(String uhid, String mealType);


    /**
     * Create à la carte order (no time or meal restrictions)
     *
     * @param uhid Patient UHID
     * @param request À la carte order request with food item IDs
     * @return Created meal order response
     */
    MealOrderResponse createAlacarteOrder(String uhid, AlacarteOrderRequest request);

    /**
     * Get à la carte menu items (available to all patients)
     */
    List<AvailableMenuResponse.FoodOption> getAlacarteMenu();

}