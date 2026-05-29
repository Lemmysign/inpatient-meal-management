package com.hospital.meal.controller;

import com.hospital.meal.constant.ApiConstants;
import com.hospital.meal.dto.admin.AdminDashboardResponse;
import com.hospital.meal.dto.common.ApiResponse;
import com.hospital.meal.service.admin.AdminDashboardService;
import com.hospital.meal.util.DateUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping(ApiConstants.ADMIN_BASE)
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Dashboard", description = "Admin dashboard and analytics endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final AdminDashboardService adminDashboardService;

    /**
     * Get admin dashboard metrics
     */
    @GetMapping(ApiConstants.ADMIN_DASHBOARD)
    @Operation(summary = "Get Admin Dashboard", description = "Get comprehensive dashboard metrics for admin")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getAdminDashboard(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        log.info("Get admin dashboard request");

        LocalDate targetDate = date != null ? LocalDate.parse(date) : DateUtil.getCurrentDate();
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;

        AdminDashboardResponse dashboard = adminDashboardService.getDashboardMetrics(
                targetDate,
                start,
                end
        );

        log.info("Admin dashboard metrics retrieved successfully");

        return ResponseEntity.ok(
                ApiResponse.success("Dashboard metrics retrieved successfully", dashboard)
        );
    }

    /**
     * Get today's dashboard (convenience method)
     */
    @GetMapping(ApiConstants.ADMIN_DASHBOARD + "/today")
    @Operation(summary = "Get Today's Dashboard", description = "Get dashboard metrics for today")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getTodayDashboard() {

        log.info("Get today's dashboard request");

        LocalDate today = DateUtil.getCurrentDate();

        AdminDashboardResponse dashboard = adminDashboardService.getDashboardMetrics(
                today,
                null,
                null
        );

        return ResponseEntity.ok(
                ApiResponse.success("Today's dashboard retrieved successfully", dashboard)
        );
    }

    /**
     * Get weekly dashboard
     */
    @GetMapping(ApiConstants.ADMIN_DASHBOARD + "/weekly")
    @Operation(summary = "Get Weekly Dashboard", description = "Get dashboard metrics for the past 7 days")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getWeeklyDashboard() {

        log.info("Get weekly dashboard request");

        LocalDate today = DateUtil.getCurrentDate();
        LocalDate startOfWeek = today.minusDays(6);

        AdminDashboardResponse dashboard = adminDashboardService.getDashboardMetrics(
                today,
                startOfWeek,
                today
        );

        return ResponseEntity.ok(
                ApiResponse.success("Weekly dashboard retrieved successfully", dashboard)
        );
    }

    /**
     * Get monthly dashboard
     */
    @GetMapping(ApiConstants.ADMIN_DASHBOARD + "/monthly")
    @Operation(summary = "Get Monthly Dashboard", description = "Get dashboard metrics for the current month")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getMonthlyDashboard() {

        log.info("Get monthly dashboard request");

        LocalDate today = DateUtil.getCurrentDate();
        LocalDate startOfMonth = today.withDayOfMonth(1);

        AdminDashboardResponse dashboard = adminDashboardService.getDashboardMetrics(
                today,
                startOfMonth,
                today
        );

        return ResponseEntity.ok(
                ApiResponse.success("Monthly dashboard retrieved successfully", dashboard)
        );
    }

    /**
     * Get custom date range dashboard
     */
    @GetMapping(ApiConstants.ADMIN_DASHBOARD + "/range")
    @Operation(summary = "Get Custom Range Dashboard", description = "Get dashboard metrics for a custom date range")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getCustomRangeDashboard(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        log.info("Get custom range dashboard request: {} to {}", startDate, endDate);

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        LocalDate today = DateUtil.getCurrentDate();

        // Validate date range
        if (start.isAfter(end)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Start date cannot be after end date", HttpStatus.BAD_REQUEST.value()));
        }

        AdminDashboardResponse dashboard = adminDashboardService.getDashboardMetrics(
                today,
                start,
                end
        );

        return ResponseEntity.ok(
                ApiResponse.success("Custom range dashboard retrieved successfully", dashboard)
        );
    }
}