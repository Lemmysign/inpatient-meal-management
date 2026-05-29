package com.hospital.meal.service.service_impl.dietician;

import com.hospital.meal.dto.common.PageResponse;
import com.hospital.meal.dto.dietician.CreateFoodItemRequest;
import com.hospital.meal.dto.dietician.FoodItemResponse;
import com.hospital.meal.exception.ResourceNotFoundException;
import com.hospital.meal.mapper.FoodItemMapper;
import com.hospital.meal.model.menu.FoodItem;
import com.hospital.meal.model.menu.MenuGroup;
import com.hospital.meal.repository.FoodItemRepository;
import com.hospital.meal.repository.MenuGroupRepository;
import com.hospital.meal.service.dietician.FoodItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodItemServiceImpl implements FoodItemService {

    private final FoodItemRepository foodItemRepository;
    private final MenuGroupRepository menuGroupRepository;
    private final FoodItemMapper foodItemMapper;

    @Override
    @Transactional
    public FoodItemResponse createFoodItem(CreateFoodItemRequest request) {
        log.info("Creating food item: {} for menu group: {}", request.getName(), request.getMenuGroupId());

        // Validate menu group exists and is active
        MenuGroup menuGroup = menuGroupRepository.findById(request.getMenuGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu Group", "id", request.getMenuGroupId()));

        if (!menuGroup.getIsActive()) {
            throw new IllegalStateException("Cannot add food items to inactive menu group");
        }

        // Create food item
        FoodItem foodItem = FoodItem.builder()
                .menuGroup(menuGroup)
                .name(request.getName())
                .description(request.getDescription())
                .mealType(request.getMealType()) // Can be null for items available for any meal
                .isActive(true)
                .build();

        foodItem = foodItemRepository.save(foodItem);

        log.info("Food item created successfully with ID: {}", foodItem.getId());

        return foodItemMapper.toResponse(foodItem);
    }

    @Override
    @Transactional
    public FoodItemResponse updateFoodItem(UUID foodItemId, CreateFoodItemRequest request) {
        log.info("Updating food item: {}", foodItemId);

        // Get food item
        FoodItem foodItem = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Food Item", "id", foodItemId));

        // Update menu group if changed
        if (!foodItem.getMenuGroup().getId().equals(request.getMenuGroupId())) {
            MenuGroup newMenuGroup = menuGroupRepository.findById(request.getMenuGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu Group", "id", request.getMenuGroupId()));

            if (!newMenuGroup.getIsActive()) {
                throw new IllegalStateException("Cannot move food item to inactive menu group");
            }

            foodItem.setMenuGroup(newMenuGroup);
        }

        // Update fields
        foodItem.setName(request.getName());
        foodItem.setDescription(request.getDescription());
        foodItem.setMealType(request.getMealType());

        foodItem = foodItemRepository.save(foodItem);

        log.info("Food item updated successfully");

        return foodItemMapper.toResponse(foodItem);
    }

    @Override
    @Transactional
    public void deleteFoodItem(UUID foodItemId) {
        log.info("Deleting food item: {}", foodItemId);

        // Get food item
        FoodItem foodItem = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Food Item", "id", foodItemId));

        // Soft delete - deactivate instead of hard delete
        // This preserves historical data for orders that used this food item
        foodItem.setIsActive(false);
        foodItemRepository.save(foodItem);

        log.info("Food item deleted (deactivated) successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public FoodItemResponse getFoodItemById(UUID foodItemId) {
        log.debug("Getting food item by ID: {}", foodItemId);

        FoodItem foodItem = foodItemRepository.findById(foodItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Food Item", "id", foodItemId));

        return foodItemMapper.toResponse(foodItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodItemResponse> getFoodItemsByMenuGroup(UUID menuGroupId) {
        log.debug("Getting food items for menu group: {}", menuGroupId);

        // Validate menu group exists
        if (!menuGroupRepository.existsById(menuGroupId)) {
            throw new ResourceNotFoundException("Menu Group", "id", menuGroupId);
        }

        return foodItemRepository.findByMenuGroupId(menuGroupId).stream()
                .map(foodItemMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodItemResponse> getFoodItemsByMenuGroupAndMealType(UUID menuGroupId, String mealType) {
        log.debug("Getting food items for menu group: {} and meal type: {}", menuGroupId, mealType);

        // Validate menu group exists
        if (!menuGroupRepository.existsById(menuGroupId)) {
            throw new ResourceNotFoundException("Menu Group", "id", menuGroupId);
        }

        return foodItemRepository.findByMenuGroupIdAndMealType(menuGroupId, mealType).stream()
                .map(foodItemMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodItemResponse> getFoodItemsByMealType(String mealType) {
        log.debug("Getting food items by meal type: {}", mealType);

        return foodItemRepository.findByMealType(mealType).stream()
                .map(foodItemMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FoodItemResponse> searchFoodItems(String search, Pageable pageable) {
        log.debug("Searching food items with query: {}", search);

        Page<FoodItem> foodItemPage = foodItemRepository.searchFoodItems(search, pageable);

        return PageResponse.<FoodItemResponse>builder()
                .content(foodItemPage.getContent().stream()
                        .map(foodItemMapper::toResponse)
                        .collect(Collectors.toList()))
                .page(foodItemPage.getNumber())
                .size(foodItemPage.getSize())
                .totalElements(foodItemPage.getTotalElements())
                .totalPages(foodItemPage.getTotalPages())
                .first(foodItemPage.isFirst())
                .last(foodItemPage.isLast())
                .empty(foodItemPage.isEmpty())
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public PageResponse<FoodItemResponse> getAllFoodItems(UUID menuGroupId, String mealType, Pageable pageable) {
        log.info("Getting all food items - menuGroupId: {}, mealType: {}, page: {}, size: {}",
                menuGroupId, mealType, pageable.getPageNumber(), pageable.getPageSize());

        Page<FoodItem> foodItemsPage;

        // Apply filters based on parameters
        if (menuGroupId != null && mealType != null) {
            // Both filters
            foodItemsPage = foodItemRepository.findByMenuGroupIdAndMealTypePaged(
                    menuGroupId, mealType, pageable);
        } else if (menuGroupId != null) {
            // Menu group filter only
            foodItemsPage = foodItemRepository.findByMenuGroupIdPaged(menuGroupId, pageable);
        } else if (mealType != null) {
            // Meal type filter only
            foodItemsPage = foodItemRepository.findByMealTypePaged(mealType, pageable);
        } else {
            // No filters - all active food items
            foodItemsPage = foodItemRepository.findAllActive(pageable);
        }

        return PageResponse.<FoodItemResponse>builder()
                .content(foodItemsPage.getContent().stream()
                        .map(foodItemMapper::toResponse)
                        .collect(Collectors.toList()))
                .page(foodItemsPage.getNumber())
                .size(foodItemsPage.getSize())
                .totalElements(foodItemsPage.getTotalElements())
                .totalPages(foodItemsPage.getTotalPages())
                .first(foodItemsPage.isFirst())
                .last(foodItemsPage.isLast())
                .empty(foodItemsPage.isEmpty())
                .build();
    }




}