package com.hospital.meal.dto.patient;

import lombok.*;

/**
 * Holds patient info fetched from HIS or any PatientInfoProvider.
 * firstName + lastName are kept separate so the scraper can map directly,
 * but a convenience method returns the full name for Patient.name field.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientInfo {

    private String firstName;
    private String lastName;
    private String roomNumber;

    /**
     * Returns full name as "FIRSTNAME LASTNAME"
     * Used when saving to Patient.name
     */
    public String getFullName() {
        if (firstName == null && lastName == null) return null;
        if (firstName == null) return lastName;
        if (lastName == null) return firstName;
        return (firstName + " " + lastName).trim();
    }
}