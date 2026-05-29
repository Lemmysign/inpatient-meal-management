package com.hospital.meal.dto.dietician;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientMenuSearchRequest {

    private List<UUID> menuGroupIds;      // Filter by menu groups (multi-select)
    private List<UUID> dieticianIds;      // Filter by dieticians (multi-select)
    private String searchTerm;            // Search patient name or UHID
    private LocalDate dateFrom;           // Filter by validFrom >= dateFrom
    private LocalDate dateTo;             // Filter by validUntil <= dateTo
    private Boolean isActive;             // Filter by active status

    // Pagination
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "assignedAt";
    private String sortDir = "DESC";
}