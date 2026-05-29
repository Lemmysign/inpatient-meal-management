package com.hospital.meal.service.service_impl.dietician;

import com.hospital.meal.dto.dietician.DieticianDashboardResponse;
import com.hospital.meal.exception.ResourceNotFoundException;
import com.hospital.meal.model.menu.PatientMenu;
import com.hospital.meal.model.order.MealOrder;
import com.hospital.meal.repository.*;
import com.hospital.meal.service.dietician.DieticianDashboardService;
import com.hospital.meal.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DieticianDashboardServiceImpl implements DieticianDashboardService {

    private final DieticianRepository dieticianRepository;
    private final PatientMenuRepository patientMenuRepository;
    private final MenuGroupRepository menuGroupRepository;
    private final MealOrderRepository mealOrderRepository;
    private final MealOrderItemRepository mealOrderItemRepository;

    @Override
    @Transactional(readOnly = true)
    public DieticianDashboardResponse getDashboardMetrics(UUID dieticianId, LocalDate date) {
        log.info("Generating dietician dashboard metrics for dietician: {} on date: {}", dieticianId, date);

        if (!dieticianRepository.existsById(dieticianId)) {
            throw new ResourceNotFoundException("Dietician", "id", dieticianId);
        }

        LocalDate targetDate = date != null ? date : DateUtil.getCurrentDate();

        // ✅ Fetch patient menus ONCE — replaces 3 separate calls
        List<PatientMenu> patientMenus = patientMenuRepository.findActiveMenusByDieticianId(dieticianId);

        // ✅ Total unique patients — derived from the single fetch above
        long totalPatientsAssigned = patientMenus.stream()
                .map(pm -> pm.getPatient().getId())
                .distinct()
                .count();

        // ✅ Count query only — no list loaded into memory
        long totalMenuGroupsCreated = menuGroupRepository.countByDieticianId(dieticianId);

        // ✅ Filter valid menus for target date in memory
        Set<UUID> validPatientIds = patientMenus.stream()
                .filter(pm -> isMenuValidForDate(pm, targetDate))
                .map(pm -> pm.getPatient().getId())
                .collect(Collectors.toSet());

        long totalOrdersToday = 0L;
        List<DieticianDashboardResponse.RecentPatientOrder> recentOrders = Collections.emptyList();

        if (!validPatientIds.isEmpty()) {
            // ✅ Single count query instead of loading all orders and filtering in Java
            totalOrdersToday = mealOrderRepository.countByPatientIdsAndDate(validPatientIds, targetDate);

            // ✅ Single query for 7 days of orders with patient and items eagerly loaded
            // — replaces the while loop that fired up to 7 separate queries
            List<MealOrder> recentMealOrders = mealOrderRepository.findByPatientIdsAndDateRange(
                    validPatientIds,
                    targetDate.minusDays(6),
                    targetDate
            );

            recentOrders = recentMealOrders.stream()
                    .limit(10)
                    .map(order -> DieticianDashboardResponse.RecentPatientOrder.builder()
                            .uhid(order.getPatient().getUhid())
                            .patientName(order.getPatient().getName())
                            .roomNumber(order.getPatient().getRoomNumber())
                            .orderDate(order.getOrderDate())
                            .totalMeals(order.getItems().size()) // ✅ no lazy load, items already JOIN FETCHed
                            .build())
                    .collect(Collectors.toList());
        }

        log.info("Dashboard metrics generated successfully for dietician");

        return DieticianDashboardResponse.builder()
                .date(targetDate)
                .totalPatientsAssigned(totalPatientsAssigned)
                .totalMenuGroupsCreated(totalMenuGroupsCreated)
                .totalOrdersToday(totalOrdersToday)
                .recentOrders(recentOrders)
                .build();
    }

    private boolean isMenuValidForDate(PatientMenu pm, LocalDate date) {
        return !pm.getValidFrom().isAfter(date)
                && (pm.getValidUntil() == null || !pm.getValidUntil().isBefore(date));
    }
}