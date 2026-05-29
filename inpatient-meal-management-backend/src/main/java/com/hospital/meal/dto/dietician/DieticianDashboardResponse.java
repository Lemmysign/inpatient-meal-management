package com.hospital.meal.dto.dietician;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DieticianDashboardResponse {

    private LocalDate date;
    private Long totalPatientsAssigned;
    private Long totalMenuGroupsCreated;
    private Long totalOrdersToday;
    private List<RecentPatientOrder> recentOrders;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentPatientOrder {
        private String uhid;
        private String patientName;
        private String roomNumber;
        private LocalDate orderDate;
        private Integer totalMeals;
    }
}