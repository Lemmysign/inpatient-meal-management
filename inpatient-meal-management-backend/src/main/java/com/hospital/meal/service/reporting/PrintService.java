package com.hospital.meal.service.reporting;

import com.hospital.meal.dto.kitchen.PrintLabelResponse;

import java.util.UUID;

public interface PrintService {

    /**
     * Generate print label for a processed meal
     */
    PrintLabelResponse generateMealLabel(UUID mealItemId);

    /**
     * Generate HTML label for 70x35mm thermal printer
     */
    String generateLabelHtml(PrintLabelResponse label);
}