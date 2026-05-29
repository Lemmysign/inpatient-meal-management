package com.hospital.meal.util;

import com.hospital.meal.dto.kitchen.PrintLabelResponse;

import java.time.format.DateTimeFormatter;

public final class PrintLabelGenerator {

    private PrintLabelGenerator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Generate HTML for 70mm x 35mm thermal printer label
     */
    public static String generateLabelHtml(PrintLabelResponse labelData) {
        if (labelData == null) {
            throw new IllegalArgumentException("Label data cannot be null");
        }

        String processedTime = labelData.getProcessedAt() != null ?
                labelData.getProcessedAt().format(TIME_FORMATTER) : "";

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    @page {
                        size: 70mm 35mm;
                        margin: 0;
                    }
                    body {
                        width: 70mm;
                        height: 35mm;
                        margin: 0;
                        padding: 2mm;
                        font-family: Arial, sans-serif;
                        font-size: 10pt;
                        line-height: 1.2;
                    }
                    .header {
                        font-weight: bold;
                        font-size: 12pt;
                        border-bottom: 2px solid #000;
                        padding-bottom: 1mm;
                        margin-bottom: 1mm;
                    }
                    .row {
                        margin-bottom: 0.5mm;
                    }
                    .label {
                        font-weight: bold;
                        display: inline-block;
                        width: 20mm;
                    }
                    .value {
                        display: inline-block;
                    }
                    .meal-type {
                        font-size: 11pt;
                        font-weight: bold;
                        text-transform: uppercase;
                    }
                    .status {
                        position: absolute;
                        top: 2mm;
                        right: 2mm;
                        background: #000;
                        color: #fff;
                        padding: 1mm 2mm;
                        font-size: 8pt;
                        font-weight: bold;
                    }
                </style>
            </head>
            <body>
                <div class="status">%s</div>
                <div class="header">HOSPITAL MEAL</div>
                <div class="row">
                    <span class="label">UHID:</span>
                    <span class="value">%s</span>
                </div>
                <div class="row">
                    <span class="label">Patient:</span>
                    <span class="value">%s</span>
                </div>
                <div class="row">
                    <span class="label">Room:</span>
                    <span class="value">%s</span>
                </div>
                <div class="row">
                    <span class="label">Meal:</span>
                    <span class="value meal-type">%s</span>
                </div>
                <div class="row">
                    <span class="label">Food:</span>
                    <span class="value">%s</span>
                </div>
                <div class="row">
                    <span class="label">Time:</span>
                    <span class="value">%s</span>
                </div>
            </body>
            </html>
            """.formatted(
                labelData.getStatus(),
                labelData.getUhid(),
                labelData.getPatientName(),
                labelData.getRoomNumber(),
                labelData.getMealType(),
                labelData.getFoodItemName(),
                processedTime
        );
    }
}