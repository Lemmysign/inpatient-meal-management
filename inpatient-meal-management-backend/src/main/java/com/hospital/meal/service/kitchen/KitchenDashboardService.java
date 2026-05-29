package com.hospital.meal.service.kitchen;

import com.hospital.meal.dto.kitchen.KitchenDashboardResponse;

import java.time.LocalDate;

public interface KitchenDashboardService {

    /**
     * Get kitchen dashboard metrics for a specific date
     */
    KitchenDashboardResponse getDashboardMetrics(LocalDate date);
}