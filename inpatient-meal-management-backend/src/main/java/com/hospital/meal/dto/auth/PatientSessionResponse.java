package com.hospital.meal.dto.auth;

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
public class PatientSessionResponse {

    private String sessionToken;
    private UUID patientId;
    private String uhid;
    private String name;
    private String roomNumber;
    private LocalDateTime expiresAt;
    private boolean hasActiveMenu;
}