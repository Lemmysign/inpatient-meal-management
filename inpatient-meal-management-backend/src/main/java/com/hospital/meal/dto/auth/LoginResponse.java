package com.hospital.meal.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn; // in milliseconds
    private UUID userId;
    private String name;
    private String email;
    private String role; // ADMIN, DIETICIAN, KITCHEN_STAFF
}