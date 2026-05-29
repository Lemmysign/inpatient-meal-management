package com.hospital.meal.service.dietician;

import com.hospital.meal.dto.dietician.DieticianDashboardResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface DieticianDashboardService {

    /**
     * Get dietician dashboard metrics
     */
    DieticianDashboardResponse getDashboardMetrics(UUID dieticianId, LocalDate date);
}