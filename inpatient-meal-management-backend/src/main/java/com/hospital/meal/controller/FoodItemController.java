package com.hospital.meal.controller;

import com.hospital.meal.constant.ApiConstants;
import com.hospital.meal.dto.common.ApiResponse;
import com.hospital.meal.dto.common.PageResponse;
import com.hospital.meal.dto.dietician.CreateFoodItemRequest;
import com.hospital.meal.dto.dietician.FoodItemResponse;
import com.hospital.meal.service.dietician.FoodItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.DIETICIAN_BASE + ApiConstants.DIETICIAN_FOOD_ITEMS)
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Food Item", description = "Food item management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class FoodItemController {

    private final FoodItemService foodItemService;

    /**
     * Create new food item
     */
    @PostMapping
    @Operation(summary = "Create Food Item", description = "Create a new food item in a menu group")
    public ResponseEntity<ApiResponse<FoodItemResponse>> createFoodItem(
            @Valid @RequestBody CreateFoodItemRequest request) {

        log.info("Create food item request: {} for menu group: {}",
                request.getName(), request.getMenuGroupId());

        FoodItemResponse foodItem = foodItemService.createFoodItem(request);

        log.info("Food item created successfully: {}", foodItem.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Food item created successfully", foodItem));
    }

    /**
     * Update food item
     */
    @PutMapping("/{foodItemId}")
    @Operation(summary = "Update Food Item", description = "Update an existing food item")
    public ResponseEntity<ApiResponse<FoodItemResponse>> updateFoodItem(
            @PathVariable UUID foodItemId,
            @Valid @RequestBody CreateFoodItemRequest request) {

        log.info("Update food item request: {}", foodItemId);

        FoodItemResponse foodItem = foodItemService.updateFoodItem(foodItemId, request);

        log.info("Food item updated successfully");

        return ResponseEntity.ok(
                ApiResponse.success("Food item updated successfully", foodItem)
        );
    }

    /**
     * Delete food item
     */
    @DeleteMapping("/{foodItemId}")
    @Operation(summary = "Delete Food Item", description = "Delete a food item (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteFoodItem(
            @PathVariable UUID foodItemId) {

        log.info("Delete food item request: {}", foodItemId);

        foodItemService.deleteFoodItem(foodItemId);

        log.info("Food item deleted successfully");

        return ResponseEntity.ok(
                ApiResponse.success("Food item deleted successfully", null)
        );
    }

    /**
     * Get food item by ID
     */
    @GetMapping("/{foodItemId}")
    @Operation(summary = "Get Food Item", description = "Get food item details by ID")
    public ResponseEntity<ApiResponse<FoodItemResponse>> getFoodItem(
            @PathVariable UUID foodItemId) {

        log.info("Get food item request: {}", foodItemId);

        FoodItemResponse foodItem = foodItemService.getFoodItemById(foodItemId);

        return ResponseEntity.ok(
                ApiResponse.success("Food item retrieved successfully", foodItem)
        );
    }

    /**
     * Get food items by menu group
     */
    @GetMapping("/menu-group/{menuGroupId}")
    @Operation(summary = "Get Food Items by Menu Group", description = "Get all food items in a specific menu group")
    public ResponseEntity<ApiResponse<List<FoodItemResponse>>> getFoodItemsByMenuGroup(
            @PathVariable UUID menuGroupId) {

        log.info("Get food items for menu group: {}", menuGroupId);

        List<FoodItemResponse> foodItems = foodItemService.getFoodItemsByMenuGroup(menuGroupId);

        return ResponseEntity.ok(
                ApiResponse.success("Food items retrieved successfully", foodItems)
        );
    }

    /**
     * Get food items by menu group and meal type
     */
    @GetMapping("/menu-group/{menuGroupId}/meal-type/{mealType}")
    @Operation(summary = "Get Food Items by Menu Group and Meal Type",
            description = "Get food items filtered by menu group and meal type (BREAKFAST, LUNCH, DINNER)")
    public ResponseEntity<ApiResponse<List<FoodItemResponse>>> getFoodItemsByMenuGroupAndMealType(
            @PathVariable UUID menuGroupId,
            @PathVariable String mealType) {

        log.info("Get food items for menu group: {} and meal type: {}", menuGroupId, mealType);

        List<FoodItemResponse> foodItems = foodItemService.getFoodItemsByMenuGroupAndMealType(
                menuGroupId,
                mealType.toUpperCase()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Food items retrieved successfully", foodItems)
        );
    }

    /**
     * Get food items by meal type (across all menu groups)
     */
    @GetMapping("/meal-type/{mealType}")
    @Operation(summary = "Get Food Items by Meal Type",
            description = "Get all food items for a specific meal type across all menu groups")
    public ResponseEntity<ApiResponse<List<FoodItemResponse>>> getFoodItemsByMealType(
            @PathVariable String mealType) {

        log.info("Get food items for meal type: {}", mealType);

        List<FoodItemResponse> foodItems = foodItemService.getFoodItemsByMealType(mealType.toUpperCase());

        return ResponseEntity.ok(
                ApiResponse.success("Food items retrieved successfully", foodItems)
        );
    }

    /**
     * Search food items
     */
    @GetMapping("/search")
    @Operation(summary = "Search Food Items", description = "Search food items by name or description")
    public ResponseEntity<ApiResponse<PageResponse<FoodItemResponse>>> searchFoodItems(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Search food items request: {}", query);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));

        PageResponse<FoodItemResponse> foodItems = foodItemService.searchFoodItems(query, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Search results retrieved successfully", foodItems)
        );
    }


    /**
     * Get all food items with optional filters
     */
    @GetMapping
    @Operation(
            summary = "Get all food items",
            description = "Get all active food items with optional filtering by menu group and meal type"
    )
    public ResponseEntity<ApiResponse<PageResponse<FoodItemResponse>>> getAllFoodItems(
            @RequestParam(required = false) UUID menuGroupId,
            @RequestParam(required = false) String mealType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir
    ) {
        log.info("Getting all food items - menuGroupId: {}, mealType: {}, page: {}, size: {}",
                menuGroupId, mealType, page, size);

        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());

        PageResponse<FoodItemResponse> foodItems = foodItemService.getAllFoodItems(
                menuGroupId, mealType, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Food items retrieved successfully", foodItems)
        );
    }




}