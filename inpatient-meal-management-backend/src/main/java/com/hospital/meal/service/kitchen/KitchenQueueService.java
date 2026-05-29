package com.hospital.meal.service.kitchen;

import com.hospital.meal.controller.KitchenStaffController;
import com.hospital.meal.dto.kitchen.KitchenQueueResponse;
import com.hospital.meal.dto.kitchen.MealQueueItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface KitchenQueueService {

    /**
     * Get kitchen queue by meal type
     */
    KitchenQueueResponse getQueueByMealType(String mealType, String status);

    /**
     * Get all pending meals grouped by type
     */
    List<KitchenQueueResponse> getAllPendingQueues();

    /**
     * Get queue with pagination
     */
    KitchenQueueResponse getQueueByMealTypePaged(String mealType, String status, Pageable pageable);



    /**
     * Get paginated queue for specific meal type
     */
    Page<MealQueueItemResponse> getQueuePaginated(String mealType, String statusCode, Pageable pageable);

    /**
     * Get paginated queue for all meal types
     */
    Map<String, Page<MealQueueItemResponse>> getAllQueuesPaginated(String statusCode, Pageable pageable);



    /**
     * Get paginated queue with search
     */
    Page<MealQueueItemResponse> getQueuePaginatedWithSearch(
            String mealType,
            String statusCode,
            String searchTerm,
            Pageable pageable);

    /**
     * Get all queues paginated with search
     */
    Map<String, Page<MealQueueItemResponse>> getAllQueuesPaginatedWithSearch(
            String statusCode,
            String searchTerm,
            Pageable pageable);


}