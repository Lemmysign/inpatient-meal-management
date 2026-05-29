package com.hospital.meal.dto.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KitchenStaffResponse {

    private UUID id;
    private String name;
    private String email;
    private String phoneNumber;
    private Boolean isActive;
    private Integer sessionDurationHours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}