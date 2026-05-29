package com.hospital.meal.service.admin;

import com.hospital.meal.dto.admin.AdminDashboardResponse;

import java.time.LocalDate;

public interface AdminDashboardService {

    /**
     * Get comprehensive admin dashboard metrics
     */
    AdminDashboardResponse getDashboardMetrics(LocalDate date, LocalDate startDate, LocalDate endDate);
}