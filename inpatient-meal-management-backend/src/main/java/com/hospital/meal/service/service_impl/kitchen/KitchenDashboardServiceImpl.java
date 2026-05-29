package com.hospital.meal.service.service_impl.kitchen;

import com.hospital.meal.constant.MealStatusConstants;
import com.hospital.meal.constant.MealTypeConstants;
import com.hospital.meal.dto.kitchen.KitchenDashboardResponse;
import com.hospital.meal.repository.MealOrderItemRepository;
import com.hospital.meal.repository.MealOrderRepository;
import com.hospital.meal.service.kitchen.KitchenDashboardService;
import com.hospital.meal.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KitchenDashboardServiceImpl implements KitchenDashboardService {

    private final MealOrderRepository mealOrderRepository;
    private final MealOrderItemRepository mealOrderItemRepository;

    @Override
    @Transactional(readOnly = true)
    public KitchenDashboardResponse getDashboardMetrics(LocalDate date) {
        log.info("Generating kitchen dashboard metrics for date: {}", date);

        // Use today if date not provided
        LocalDate targetDate = date != null ? date : DateUtil.getCurrentDate();

        // Total orders today
        Long totalOrdersToday = getTotalOrdersForDate(targetDate);

        // Pending meals by type
        Long pendingBreakfast = getPendingMealsByType(targetDate, MealTypeConstants.BREAKFAST);
        Long pendingLunch = getPendingMealsByType(targetDate, MealTypeConstants.LUNCH);
        Long pendingDinner = getPendingMealsByType(targetDate, MealTypeConstants.DINNER);

        // Total processed meals
        Long processedMeals = getProcessedMealsForDate(targetDate);

        // Average processing time
        Double averageProcessingTimeMinutes = getAverageProcessingTime(targetDate);

        // Meals by status
        Map<String, Long> mealsByStatus = getMealsByStatus(targetDate);

        // Meals by type
        Map<String, Long> mealsByType = getMealsByType(targetDate);

        KitchenDashboardResponse response = KitchenDashboardResponse.builder()
                .date(targetDate)
                .totalOrdersToday(totalOrdersToday)
                .pendingBreakfast(pendingBreakfast)
                .pendingLunch(pendingLunch)
                .pendingDinner(pendingDinner)
                .processedMeals(processedMeals)
                .averageProcessingTimeMinutes(averageProcessingTimeMinutes)
                .mealsByStatus(mealsByStatus)
                .mealsByType(mealsByType)
                .build();

        log.info("Kitchen dashboard metrics generated successfully");

        return response;
    }

    private Long getTotalOrdersForDate(LocalDate date) {
        return mealOrderRepository.countByOrderDate(date);
    }

    private Long getPendingMealsByType(LocalDate date, String mealType) {
        // Get all pending meals of this type for the date
        List<Object[]> results = mealOrderItemRepository.countByStatusAndMealTypeForDate(date);

        for (Object[] row : results) {
            String statusCode = (String) row[0];
            String type = (String) row[1];
            Long count = ((Number) row[2]).longValue();

            if (MealStatusConstants.PENDING.equals(statusCode) && mealType.equals(type)) {
                return count;
            }
        }

        return 0L;
    }

    private Long getProcessedMealsForDate(LocalDate date) {
        List<Object[]> results = mealOrderItemRepository.countByStatusForDate(date);

        for (Object[] row : results) {
            String statusCode = (String) row[0];
            Long count = ((Number) row[1]).longValue();

            if (MealStatusConstants.PROCESSED.equals(statusCode)) {
                return count;
            }
        }

        return 0L;
    }

    private Double getAverageProcessingTime(LocalDate date) {
        Double avgTime = mealOrderItemRepository.calculateAverageProcessingTimeForDate(date);

        // Round to 2 decimal places
        if (avgTime != null) {
            return Math.round(avgTime * 100.0) / 100.0;
        }

        return 0.0;
    }

    private Map<String, Long> getMealsByStatus(LocalDate date) {
        List<Object[]> results = mealOrderItemRepository.countByStatusForDate(date);

        Map<String, Long> mealsByStatus = new HashMap<>();

        // Initialize all statuses with 0
        mealsByStatus.put(MealStatusConstants.PENDING, 0L);
        mealsByStatus.put(MealStatusConstants.PROCESSING, 0L);
        mealsByStatus.put(MealStatusConstants.PROCESSED, 0L);

        // Fill in actual counts
        for (Object[] row : results) {
            String statusCode = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            mealsByStatus.put(statusCode, count);
        }

        return mealsByStatus;
    }

    private Map<String, Long> getMealsByType(LocalDate date) {
        List<Object[]> results = mealOrderItemRepository.countByMealTypeForDate(date);

        Map<String, Long> mealsByType = new HashMap<>();

        // Initialize all meal types with 0
        mealsByType.put(MealTypeConstants.BREAKFAST, 0L);
        mealsByType.put(MealTypeConstants.LUNCH, 0L);
        mealsByType.put(MealTypeConstants.DINNER, 0L);
        mealsByType.put(MealTypeConstants.EXTRA, 0L);

        // Fill in actual counts
        for (Object[] row : results) {
            String mealType = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            mealsByType.put(mealType, count);
        }

        return mealsByType;
    }

    /**
     * Get dashboard metrics with additional breakdown
     */
    @Transactional(readOnly = true)
    public KitchenDashboardResponse getDashboardMetricsDetailed(LocalDate date) {
        KitchenDashboardResponse basicMetrics = getDashboardMetrics(date);

        // Can add more detailed metrics here if needed
        // For example: pending meals per hour, staff performance, etc.

        return basicMetrics;
    }

    /**
     * Get processing efficiency metrics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getProcessingEfficiencyMetrics(LocalDate date) {
        LocalDate targetDate = date != null ? date : DateUtil.getCurrentDate();

        Map<String, Object> metrics = new HashMap<>();

        // Total meals for the day
        List<Object[]> totalMeals = mealOrderItemRepository.countByMealTypeForDate(targetDate);
        long total = totalMeals.stream()
                .mapToLong(row -> ((Number) row[1]).longValue())
                .sum();

        metrics.put("totalMeals", total);

        // Processed count
        Long processed = getProcessedMealsForDate(targetDate);
        metrics.put("processedMeals", processed);

        // Calculate completion percentage
        double completionPercentage = total > 0 ? (processed * 100.0 / total) : 0.0;
        metrics.put("completionPercentage", Math.round(completionPercentage * 100.0) / 100.0);

        // Average processing time
        Double avgTime = getAverageProcessingTime(targetDate);
        metrics.put("averageProcessingTimeMinutes", avgTime);

        // Pending count
        Long pending = getPendingMealsByType(targetDate, MealTypeConstants.BREAKFAST)
                + getPendingMealsByType(targetDate, MealTypeConstants.LUNCH)
                + getPendingMealsByType(targetDate, MealTypeConstants.DINNER);
        metrics.put("pendingMeals", pending);

        return metrics;
    }

    /**
     * Get meal type breakdown with percentages
     */
    @Transactional(readOnly = true)
    public Map<String, Map<String, Object>> getMealTypeBreakdown(LocalDate date) {
        LocalDate targetDate = date != null ? date : DateUtil.getCurrentDate();

        Map<String, Map<String, Object>> breakdown = new HashMap<>();

        // Get total count
        List<Object[]> allMeals = mealOrderItemRepository.countByMealTypeForDate(targetDate);
        long totalCount = allMeals.stream()
                .mapToLong(row -> ((Number) row[1]).longValue())
                .sum();

        // For each meal type
        for (String mealType : MealTypeConstants.ALL_MEAL_TYPES) {
            Map<String, Object> typeStats = new HashMap<>();

            // Get count for this type
            long typeCount = allMeals.stream()
                    .filter(row -> mealType.equals(row[0]))
                    .mapToLong(row -> ((Number) row[1]).longValue())
                    .sum();

            typeStats.put("count", typeCount);

            // Calculate percentage
            double percentage = totalCount > 0 ? (typeCount * 100.0 / totalCount) : 0.0;
            typeStats.put("percentage", Math.round(percentage * 100.0) / 100.0);

            // Get pending count
            long pending = getPendingMealsByType(targetDate, mealType);
            typeStats.put("pending", pending);

            // Get processed count
            long processed = typeCount - pending;
            typeStats.put("processed", processed);

            breakdown.put(mealType, typeStats);
        }

        return breakdown;
    }

    /**
     * Get hourly order distribution
     */
    @Transactional(readOnly = true)
    public Map<Integer, Long> getHourlyOrderDistribution(LocalDate date) {
        LocalDate targetDate = date != null ? date : DateUtil.getCurrentDate();

        List<Object[]> hourlyData = mealOrderItemRepository.findPeakOrderingHoursForDate(targetDate);

        Map<Integer, Long> distribution = new HashMap<>();

        for (Object[] row : hourlyData) {
            Integer hour = ((Number) row[0]).intValue();
            Long count = ((Number) row[1]).longValue();
            distribution.put(hour, count);
        }

        return distribution;
    }
}