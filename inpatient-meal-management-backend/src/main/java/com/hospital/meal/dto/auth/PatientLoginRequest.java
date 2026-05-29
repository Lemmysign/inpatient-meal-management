package com.hospital.meal.dto.auth;

import com.hospital.meal.validation.annotation.ValidUHID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientLoginRequest {

    @NotBlank(message = "UHID is required")
    @ValidUHID
    private String uhid;

    /**
     * Required only in manual mode.
     * In HIS mode, name is fetched automatically from HIS.
     */
    @Pattern(regexp = "^[a-zA-Z\\s.'-]+$", message = "Name contains invalid characters")
    private String name;

    /**
     * Required only in manual mode.
     * In HIS mode, room number is fetched automatically from HIS.
     */
    private String roomNumber;
}