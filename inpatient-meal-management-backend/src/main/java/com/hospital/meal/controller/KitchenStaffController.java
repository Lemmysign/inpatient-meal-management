package com.hospital.meal.controller;

import com.hospital.meal.constant.ApiConstants;
import com.hospital.meal.dto.common.ApiResponse;
import com.hospital.meal.dto.kitchen.KitchenDashboardResponse;
import com.hospital.meal.dto.kitchen.KitchenQueueResponse;
import com.hospital.meal.dto.kitchen.MealQueueItemResponse;
import com.hospital.meal.dto.kitchen.PrintLabelResponse;
import com.hospital.meal.dto.kitchen.ProcessMealRequest;
import com.hospital.meal.service.kitchen.KitchenDashboardService;
import com.hospital.meal.service.kitchen.KitchenQueueService;
import com.hospital.meal.service.kitchen.MealProcessingService;
import com.hospital.meal.service.reporting.PrintService;
import com.hospital.meal.util.DateUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.KITCHEN_BASE)
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Kitchen Staff", description = "Kitchen staff meal processing endpoints")
@SecurityRequirement(name = "bearerAuth")
public class KitchenStaffController {

    private final KitchenQueueService kitchenQueueService;
    private final MealProcessingService mealProcessingService;
    private final KitchenDashboardService kitchenDashboardService;
    private final PrintService printService;

    /**
     * Get kitchen dashboard metrics
     */
    @GetMapping(ApiConstants.KITCHEN_DASHBOARD)
    @Operation(summary = "Get Kitchen Dashboard", description = "Get kitchen dashboard metrics and statistics")
    public ResponseEntity<ApiResponse<KitchenDashboardResponse>> getDashboard(
            @RequestParam(required = false) String date) {

        log.info("Get kitchen dashboard request");

        LocalDate targetDate = date != null ? LocalDate.parse(date) : DateUtil.getCurrentDate();

        KitchenDashboardResponse dashboard = kitchenDashboardService.getDashboardMetrics(targetDate);

        log.info("Kitchen dashboard metrics retrieved successfully");

        return ResponseEntity.ok(
                ApiResponse.success("Dashboard metrics retrieved successfully", dashboard)
        );
    }

    /**
     * Get meal queue by meal type
     */
    @GetMapping(ApiConstants.KITCHEN_QUEUE + "/{mealType}")
    @Operation(summary = "Get Meal Queue", description = "Get pending meals queue for a specific meal type (BREAKFAST, LUNCH, DINNER, EXTRA)")
    public ResponseEntity<ApiResponse<KitchenQueueResponse>> getQueue(
            @PathVariable String mealType,
            @RequestParam(required = false) String status) {

        log.info("Get queue request for meal type: {}", mealType);

        KitchenQueueResponse queue = kitchenQueueService.getQueueByMealType(
                mealType.toUpperCase(),
                status
        );

        log.info("Queue retrieved successfully");

        return ResponseEntity.ok(
                ApiResponse.success("Queue retrieved successfully", queue)
        );
    }

    /**
     * Get all pending queues (all meal types)
     */
    @GetMapping(ApiConstants.KITCHEN_QUEUE + "/all")
    @Operation(summary = "Get All Queues", description = "Get pending queues for all meal types")
    public ResponseEntity<ApiResponse<List<KitchenQueueResponse>>> getAllQueues() {

        log.info("Get all queues request");

        List<KitchenQueueResponse> queues = kitchenQueueService.getAllPendingQueues();

        log.info("All queues retrieved successfully");

        return ResponseEntity.ok(
                ApiResponse.success("All queues retrieved successfully", queues)
        );
    }

    /**
     * Process meal (mark as processed)
     */
    @PostMapping(ApiConstants.KITCHEN_PROCESS)
    @Operation(summary = "Process Meal", description = "Process a meal item (mark as processed)")
    public ResponseEntity<ApiResponse<MealQueueItemResponse>> processMeal(
            @Valid @RequestBody ProcessMealRequest request,
            @RequestAttribute("userId") UUID kitchenStaffId) {

        log.info("Process meal request for meal item: {} by staff: {}",
                request.getMealItemId(), kitchenStaffId);

        MealQueueItemResponse processedMeal = mealProcessingService.processMeal(
                request.getMealItemId(),
                kitchenStaffId
        );

        log.info("Meal processed successfully");

        return ResponseEntity.ok(
                ApiResponse.success("Meal processed successfully", processedMeal)
        );
    }

    /**
     * Print meal label
     */
    @GetMapping(ApiConstants.KITCHEN_PRINT + "/{mealItemId}")
    @Operation(summary = "Print Meal Label", description = "Generate printable label for a meal item")
    public ResponseEntity<ApiResponse<PrintLabelResponse>> printLabel(
            @PathVariable UUID mealItemId) {

        log.info("Print label request for meal item: {}", mealItemId);

        PrintLabelResponse label = printService.generateMealLabel(mealItemId);

        log.info("Label generated successfully");

        return ResponseEntity.ok(
                ApiResponse.success("Label generated successfully", label)
        );
    }

    /**
     * Get breakfast queue (convenience method)
     */
    @GetMapping(ApiConstants.KITCHEN_QUEUE + "/breakfast")
    @Operation(summary = "Get Breakfast Queue", description = "Get pending breakfast meals")
    public ResponseEntity<ApiResponse<KitchenQueueResponse>> getBreakfastQueue() {

        log.info("Get breakfast queue request");

        KitchenQueueResponse queue = kitchenQueueService.getQueueByMealType("BREAKFAST", "PENDING");

        return ResponseEntity.ok(
                ApiResponse.success("Breakfast queue retrieved successfully", queue)
        );
    }

    /**
     * Get lunch queue (convenience method)
     */
    @GetMapping(ApiConstants.KITCHEN_QUEUE + "/lunch")
    @Operation(summary = "Get Lunch Queue", description = "Get pending lunch meals")
    public ResponseEntity<ApiResponse<KitchenQueueResponse>> getLunchQueue() {

        log.info("Get lunch queue request");

        KitchenQueueResponse queue = kitchenQueueService.getQueueByMealType("LUNCH", "PENDING");

        return ResponseEntity.ok(
                ApiResponse.success("Lunch queue retrieved successfully", queue)
        );
    }

    /**
     * Get dinner queue (convenience method)
     */
    @GetMapping(ApiConstants.KITCHEN_QUEUE + "/dinner")
    @Operation(summary = "Get Dinner Queue", description = "Get pending dinner meals")
    public ResponseEntity<ApiResponse<KitchenQueueResponse>> getDinnerQueue() {

        log.info("Get dinner queue request");

        KitchenQueueResponse queue = kitchenQueueService.getQueueByMealType("DINNER", "PENDING");

        return ResponseEntity.ok(
                ApiResponse.success("Dinner queue retrieved successfully", queue)
        );
    }

    /**
     * Get queue statistics
     */
    @GetMapping(ApiConstants.KITCHEN_QUEUE + "/stats")
    @Operation(summary = "Get Queue Statistics", description = "Get statistics for all meal queues")
    public ResponseEntity<ApiResponse<QueueStatistics>> getQueueStats() {

        log.info("Get queue statistics request");

        List<KitchenQueueResponse> queues = kitchenQueueService.getAllPendingQueues();

        int totalPending = queues.stream()
                .mapToInt(KitchenQueueResponse::getTotalCount)
                .sum();

        QueueStatistics stats = QueueStatistics.builder()
                .totalPending(totalPending)
                .breakfastPending(getQueueCount(queues, "BREAKFAST"))
                .lunchPending(getQueueCount(queues, "LUNCH"))
                .dinnerPending(getQueueCount(queues, "DINNER"))
                .extraPending(getQueueCount(queues, "EXTRA"))
                .build();

        return ResponseEntity.ok(
                ApiResponse.success("Queue statistics retrieved successfully", stats)
        );
    }

    /**
     * Helper method to get queue count for a specific meal type
     */
    private int getQueueCount(List<KitchenQueueResponse> queues, String mealType) {
        return queues.stream()
                .filter(q -> q.getMealType().equals(mealType))
                .mapToInt(KitchenQueueResponse::getTotalCount)
                .sum();
    }



    /**
     * Get paginated queue by meal type
     */
    @GetMapping("/queue/{mealType}/paged")
    @Operation(summary = "Get paginated queue by meal type")
    public ResponseEntity<ApiResponse<Page<MealQueueItemResponse>>> getQueuePaginated(
            @PathVariable String mealType,
            @RequestParam(defaultValue = "PENDING") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderedAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir
    ) {
        log.info("Fetching paginated queue for meal type: {}, status: {}, page: {}, size: {}",
                mealType, status, page, size);

        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());

        Page<MealQueueItemResponse> queue = kitchenQueueService.getQueuePaginated(mealType, status, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Paginated queue retrieved successfully", queue)
        );
    }

    /**
     * Get paginated breakfast queue
     */
    @GetMapping("/queue/breakfast/paged")
    @Operation(summary = "Get paginated breakfast queue")
    public ResponseEntity<ApiResponse<Page<MealQueueItemResponse>>> getBreakfastQueuePaginated(
            @RequestParam(defaultValue = "PENDING") String status,  // ← ADD THIS
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderedAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir
    ) {
        log.info("Fetching paginated breakfast queue, status: {}, page: {}, size: {}", status, page, size);

        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());

        Page<MealQueueItemResponse> queue = kitchenQueueService.getQueuePaginated("BREAKFAST", status, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Paginated breakfast queue retrieved successfully", queue)
        );
    }

    /**
     * Get paginated lunch queue
     */
    @GetMapping("/queue/lunch/paged")
    @Operation(summary = "Get paginated lunch queue")
    public ResponseEntity<ApiResponse<Page<MealQueueItemResponse>>> getLunchQueuePaginated(
            @RequestParam(defaultValue = "PENDING") String status,  // ← ADD THIS
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderedAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir
    ) {
        log.info("Fetching paginated lunch queue, status: {}, page: {}, size: {}", status, page, size);

        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());

        Page<MealQueueItemResponse> queue = kitchenQueueService.getQueuePaginated("LUNCH", status, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Paginated lunch queue retrieved successfully", queue)
        );
    }

    /**
     * Get paginated dinner queue
     */
    @GetMapping("/queue/dinner/paged")
    @Operation(summary = "Get paginated dinner queue")
    public ResponseEntity<ApiResponse<Page<MealQueueItemResponse>>> getDinnerQueuePaginated(
            @RequestParam(defaultValue = "PENDING") String status,  // ← ADD THIS
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderedAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir
    ) {
        log.info("Fetching paginated dinner queue, status: {}, page: {}, size: {}", status, page, size);

        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());

        Page<MealQueueItemResponse> queue = kitchenQueueService.getQueuePaginated("DINNER", status, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Paginated dinner queue retrieved successfully", queue)
        );
    }

    /**
     * Get paginated queues for all meal types
     */
    @GetMapping("/queue/all/paged")
    @Operation(summary = "Get paginated queues for all meal types")
    public ResponseEntity<ApiResponse<Map<String, Page<MealQueueItemResponse>>>> getAllQueuesPaginated(
            @RequestParam(defaultValue = "PENDING") String status,
            @RequestParam(required = false) String searchTerm,  // ← ADD THIS
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderedAt") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir
    ) {
        log.info("Fetching paginated queues for all meal types, status: {}, search: {}, page: {}, size: {}",
                status, searchTerm, page, size);

        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending());

        Map<String, Page<MealQueueItemResponse>> allQueues = kitchenQueueService.getAllQueuesPaginatedWithSearch(
                status, searchTerm, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Paginated queues retrieved successfully", allQueues)
        );
    }






    /**
     * Inner class for queue statistics response
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class QueueStatistics {
        private Integer totalPending;
        private Integer breakfastPending;
        private Integer lunchPending;
        private Integer dinnerPending;
        private Integer extraPending;
    }
}