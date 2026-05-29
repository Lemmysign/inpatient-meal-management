package com.hospital.meal.dto.patient;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlacarteOrderRequest {

    @NotEmpty(message = "At least one à la carte item must be selected")
    private List<UUID> foodItemIds;
}