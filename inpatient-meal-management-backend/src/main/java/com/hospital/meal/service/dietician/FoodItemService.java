package com.hospital.meal.service.dietician;

import com.hospital.meal.dto.dietician.CreateFoodItemRequest;
import com.hospital.meal.dto.dietician.FoodItemResponse;
import com.hospital.meal.dto.common.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface FoodItemService {

    /**
     * Create a new food item
     */
    FoodItemResponse createFoodItem(CreateFoodItemRequest request);

    /**
     * Update existing food item
     */
    FoodItemResponse updateFoodItem(UUID foodItemId, CreateFoodItemRequest request);

    /**
     * Delete (deactivate) food item
     */
    void deleteFoodItem(UUID foodItemId);

    /**
     * Get food item by ID
     */
    FoodItemResponse getFoodItemById(UUID foodItemId);

    /**
     * Get all food items for a menu group
     */
    List<FoodItemResponse> getFoodItemsByMenuGroup(UUID menuGroupId);

    /**
     * Get food items by menu group and meal type
     */
    List<FoodItemResponse> getFoodItemsByMenuGroupAndMealType(UUID menuGroupId, String mealType);

    /**
     * Get food items by meal type
     */
    List<FoodItemResponse> getFoodItemsByMealType(String mealType);

    /**
     * Search food items by name
     */
    PageResponse<FoodItemResponse> searchFoodItems(String search, Pageable pageable);


    /**
     * Get all food items with optional filters (paginated)
     */
    PageResponse<FoodItemResponse> getAllFoodItems(
            UUID menuGroupId,
            String mealType,
            Pageable pageable);

}