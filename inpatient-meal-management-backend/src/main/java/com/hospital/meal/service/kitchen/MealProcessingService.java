package com.hospital.meal.service.kitchen;

import com.hospital.meal.dto.kitchen.MealQueueItemResponse;

import java.util.UUID;

public interface MealProcessingService {

    /**
     * Process a single meal (with concurrency control)
     * Transitions status: PENDING → PROCESSING → PROCESSED
     */
    MealQueueItemResponse processMeal(UUID mealItemId, UUID kitchenStaffId);

    /**
     * Check if meal can be processed (not already processed/processing)
     */
    boolean canProcessMeal(UUID mealItemId);

    /**
     * Get meal processing status
     */
    String getMealStatus(UUID mealItemId);
}