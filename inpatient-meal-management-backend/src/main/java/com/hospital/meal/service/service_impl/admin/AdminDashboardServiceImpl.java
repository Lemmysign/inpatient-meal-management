package com.hospital.meal.service.service_impl.admin;

import com.hospital.meal.dto.admin.AdminDashboardResponse;
import com.hospital.meal.repository.*;
import com.hospital.meal.service.admin.AdminDashboardService;
import com.hospital.meal.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final MealOrderRepository mealOrderRepository;
    private final MealOrderItemRepository mealOrderItemRepository;
    private final PatientMenuRepository patientMenuRepository;
    private final DieticianRepository dieticianRepository;
    private final MenuGroupRepository menuGroupRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardMetrics(LocalDate date, LocalDate startDate, LocalDate endDate) {
        log.info("Generating admin dashboard metrics for date: {}, range: {} to {}", date, startDate, endDate);

        // Use today if date not provided
        LocalDate targetDate = date != null ? date : DateUtil.getCurrentDate();

        if (startDate == null || endDate == null) {
            endDate = targetDate;
            startDate = targetDate;  // ← today only
        }

        // ✅ FIXED: Use date range for metrics
        Long totalMealsOrdered = getTotalMealsForDateRange(startDate, endDate);
        Map<String, Long> ordersByMealType = getOrdersByMealTypeForRange(startDate, endDate);
        Map<String, Long> ordersByStatus = getOrdersByStatusForRange(startDate, endDate);
        Double averageProcessingTimeMinutes = getAverageProcessingTimeForRange(startDate, endDate);
        List<AdminDashboardResponse.PeakHourData> peakOrderingHours = getPeakOrderingHoursForRange(startDate, endDate);
        List<AdminDashboardResponse.DieticianStats> ordersPerDietician = getOrdersPerDieticianForRange(startDate, endDate);
        List<AdminDashboardResponse.MenuGroupStats> mostUsedMenuGroups = getMostUsedMenuGroups(startDate, endDate);
        List<AdminDashboardResponse.FoodItemStats> mostOrderedFoodItems = getMostOrderedFoodItems(startDate, endDate);
        List<AdminDashboardResponse.OrderVolumeData> orderVolumeTrends = getOrderVolumeTrends(startDate, endDate);

        AdminDashboardResponse response = AdminDashboardResponse.builder()
                .date(targetDate)
                .totalMealsOrdered(totalMealsOrdered)
                .ordersByMealType(ordersByMealType)
                .ordersByStatus(ordersByStatus)
                .averageProcessingTimeMinutes(averageProcessingTimeMinutes)
                .peakOrderingHours(peakOrderingHours)
                .ordersPerDietician(ordersPerDietician)
                .mostUsedMenuGroups(mostUsedMenuGroups)
                .mostOrderedFoodItems(mostOrderedFoodItems)
                .orderVolumeTrends(orderVolumeTrends)
                .build();

        log.info("Dashboard metrics generated successfully");

        return response;
    }

    // ============================================================
    // DATE RANGE METHODS (NEW)
    // ============================================================

    private Long getTotalMealsForDateRange(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = mealOrderItemRepository.countByMealTypeForDateRange(startDate, endDate);
        return results.stream()
                .mapToLong(row -> ((Number) row[1]).longValue())
                .sum();
    }

    private Map<String, Long> getOrdersByMealTypeForRange(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = mealOrderItemRepository.countByMealTypeForDateRange(startDate, endDate);

        Map<String, Long> ordersByType = new HashMap<>();
        for (Object[] row : results) {
            String mealType = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            ordersByType.put(mealType, count);
        }

        return ordersByType;
    }

    private Map<String, Long> getOrdersByStatusForRange(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = mealOrderItemRepository.countByStatusForDateRange(startDate, endDate);

        Map<String, Long> ordersByStatus = new HashMap<>();
        for (Object[] row : results) {
            String statusCode = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            ordersByStatus.put(statusCode, count);
        }

        return ordersByStatus;
    }

    private Double getAverageProcessingTimeForRange(LocalDate startDate, LocalDate endDate) {
        Double avgTime = mealOrderItemRepository.calculateAverageProcessingTimeForDateRange(startDate, endDate);
        return avgTime != null ? Math.round(avgTime * 100.0) / 100.0 : 0.0;
    }

    private List<AdminDashboardResponse.PeakHourData> getPeakOrderingHoursForRange(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = mealOrderItemRepository.findPeakOrderingHoursForDateRange(startDate, endDate);

        return results.stream()
                .limit(5) // Top 5 hours
                .map(row -> AdminDashboardResponse.PeakHourData.builder()
                        .hour(((Number) row[0]).intValue())
                        .orderCount(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    private List<AdminDashboardResponse.DieticianStats> getOrdersPerDieticianForRange(LocalDate startDate, LocalDate endDate) {
        return dieticianRepository.findAllActive().stream()
                .limit(10) // Top 10 dieticians
                .map(dietician -> {
                    // Count patients with orders in date range who have menus assigned by this dietician
                    long orderCount = patientMenuRepository.findByDieticianId(dietician.getId(), PageRequest.of(0, 1000))
                            .getContent()
                            .stream()
                            .filter(pm -> pm.getValidFrom().isBefore(endDate.plusDays(1)) &&
                                    (pm.getValidUntil() == null || pm.getValidUntil().isAfter(startDate.minusDays(1))))
                            .count();

                    return AdminDashboardResponse.DieticianStats.builder()
                            .dieticianName(dietician.getName())
                            .orderCount(orderCount)
                            .build();
                })
                .filter(stats -> stats.getOrderCount() > 0)
                .sorted((a, b) -> Long.compare(b.getOrderCount(), a.getOrderCount()))
                .collect(Collectors.toList());
    }

    // ============================================================
    // EXISTING METHODS (UNCHANGED)
    // ============================================================

    private List<AdminDashboardResponse.MenuGroupStats> getMostUsedMenuGroups(LocalDate startDate, LocalDate endDate) {
        List<AdminDashboardResponse.MenuGroupStats> stats = new ArrayList<>();

        menuGroupRepository.findAllActive().forEach(menuGroup -> {
            long usageCount = patientMenuRepository.findByMenuGroupId(menuGroup.getId())
                    .stream()
                    .filter(pm -> pm.getValidFrom().isBefore(endDate.plusDays(1)) &&
                            (pm.getValidUntil() == null || pm.getValidUntil().isAfter(startDate.minusDays(1))))
                    .count();

            if (usageCount > 0) {
                stats.add(AdminDashboardResponse.MenuGroupStats.builder()
                        .menuGroupName(menuGroup.getName())
                        .usageCount(usageCount)
                        .build());
            }
        });

        return stats.stream()
                .sorted((a, b) -> Long.compare(b.getUsageCount(), a.getUsageCount()))
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<AdminDashboardResponse.FoodItemStats> getMostOrderedFoodItems(LocalDate startDate, LocalDate endDate) {
        Pageable topTen = PageRequest.of(0, 10);
        List<Object[]> results = mealOrderItemRepository.findMostOrderedFoodItems(startDate, endDate, topTen);

        return results.stream()
                .map(row -> AdminDashboardResponse.FoodItemStats.builder()
                        .foodItemName((String) row[0])
                        .orderCount(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    private List<AdminDashboardResponse.OrderVolumeData> getOrderVolumeTrends(LocalDate startDate, LocalDate endDate) {
        List<AdminDashboardResponse.OrderVolumeData> trends = new ArrayList<>();

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            long orderCount = mealOrderRepository.countByOrderDate(currentDate);

            trends.add(AdminDashboardResponse.OrderVolumeData.builder()
                    .date(currentDate)
                    .orderCount(orderCount)
                    .build());

            currentDate = currentDate.plusDays(1);
        }

        return trends;
    }
}