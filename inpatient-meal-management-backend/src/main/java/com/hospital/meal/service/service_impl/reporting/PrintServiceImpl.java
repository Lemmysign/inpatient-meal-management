package com.hospital.meal.service.service_impl.reporting;

import com.hospital.meal.dto.kitchen.PrintLabelResponse;
import com.hospital.meal.exception.ResourceNotFoundException;
import com.hospital.meal.mapper.KitchenStaffMapper;
import com.hospital.meal.model.order.MealOrderItem;
import com.hospital.meal.repository.MealOrderItemRepository;
import com.hospital.meal.service.reporting.PrintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrintServiceImpl implements PrintService {

    private final MealOrderItemRepository mealOrderItemRepository;
    private final KitchenStaffMapper kitchenStaffMapper;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    @Transactional(readOnly = true)
    public PrintLabelResponse generateMealLabel(UUID mealItemId) {
        log.info("Generating meal label for meal item: {}", mealItemId);

        // Get meal order item
        MealOrderItem item = mealOrderItemRepository.findById(mealItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Meal Item", "id", mealItemId));

        // Map to print label response
        PrintLabelResponse label = kitchenStaffMapper.toPrintLabelResponse(item);

        // Generate HTML for label
        String labelHtml = generateLabelHtml(label);
        label.setLabelHtml(labelHtml);

        log.info("Meal label generated successfully");

        return label;
    }

    @Override
    public String generateLabelHtml(PrintLabelResponse label) {
        log.debug("Generating HTML for meal label");

        String processedTime = label.getProcessedAt() != null
                ? label.getProcessedAt().format(TIME_FORMATTER)
                : LocalDateTime.now().format(TIME_FORMATTER);

        String processedDate = label.getProcessedAt() != null
                ? label.getProcessedAt().format(DATE_FORMATTER)
                : LocalDateTime.now().format(DATE_FORMATTER);

        // Format meal type for display
        String mealTypeDisplay = formatMealType(label.getMealType());

        // HTML template for 51.2mm x 34.1mm thermal label (XPrinter - reduced 15%)
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    @page {
                        size: 51.2mm 34.1mm;
                        margin: 0;
                    }
            
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }
            
                    body {
                        width: 51.2mm;
                        height: 34.1mm;
                        font-family: 'Arial', sans-serif;
                        padding: 2mm;
                        display: flex;
                        flex-direction: column;
                        justify-content: space-between;
                    }
            
                    .header {
                        border-bottom: 2px solid #000;
                        padding-bottom: 1mm;
                        margin-bottom: 1mm;
                    }
            
                    .hospital-name {
                        font-size: 7.5pt;
                        font-weight: bold;
                        text-align: center;
                    }
            
                    .content {
                        flex: 1;
                    }
            
                    .row {
                        display: flex;
                        justify-content: space-between;
                        margin-bottom: 0.8mm;
                        font-size: 6.8pt;
                    }
            
                    .label {
                        font-weight: bold;
                        width: 35%%;
                    }
            
                    .value {
                        width: 65%%;
                        text-align: right;
                        font-weight: bold;
                    }
            
                    .footer {
                        border-top: 1px solid #000;
                        padding-top: 0.8mm;
                        font-size: 5.5pt;
                        text-align: center;
                    }
            
                    .status {
                        font-size: 6pt;
                        text-align: center;
                        font-weight: bold;
                        margin-top: 0.8mm;
                    }
            
                    @media print {
                        body {
                            print-color-adjust: exact;
                            -webkit-print-color-adjust: exact;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="hospital-name">EVERCARE CAFETERIA TICKET</div>
                </div>
            
                <div class="content">
                    <div class="row">
                        <span class="label">Meal Type:</span>
                        <span class="value">%s</span>
                    </div>
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
                        <span class="label">Food:</span>
                        <span class="value">%s</span>
                    </div>
                </div>
            
                <div class="footer">
                    <div class="status">STATUS: PROCESSED</div>
                    <div>%s | %s</div>
                </div>
            </body>
            </html>
            """,
                mealTypeDisplay,
                escapeHtml(label.getUhid()),
                escapeHtml(label.getPatientName()),
                escapeHtml(label.getRoomNumber()),
                escapeHtml(label.getFoodItemName()),
                processedDate,
                processedTime
        );
    }

    /**
     * Format meal type for display
     */
    private String formatMealType(String mealType) {
        if (mealType == null) return "MEAL";

        // Just return the meal type as-is, no emojis needed for compact row display
        return mealType;
    }

    /**
     * Escape HTML special characters
     */
    private String escapeHtml(String text) {
        if (text == null) return "";

        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Generate batch labels for multiple meal items
     */
    @Transactional(readOnly = true)
    public String generateBatchLabels(List<UUID> mealItemIds) {
        log.info("Generating batch labels for {} meal items", mealItemIds.size());

        StringBuilder batchHtml = new StringBuilder();
        batchHtml.append("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    @page {
                        size: 51.2mm 34.1mm;
                        margin: 0;
                    }
            
                    body {
                        margin: 0;
                        padding: 0;
                    }
            
                    .page-break {
                        page-break-after: always;
                    }
                </style>
            </head>
            <body>
            """);

        for (int i = 0; i < mealItemIds.size(); i++) {
            UUID mealItemId = mealItemIds.get(i);

            try {
                PrintLabelResponse label = generateMealLabel(mealItemId);

                // Add label HTML (strip html/head/body tags)
                String labelContent = extractBodyContent(label.getLabelHtml());
                batchHtml.append(labelContent);

                // Add page break except for last label
                if (i < mealItemIds.size() - 1) {
                    batchHtml.append("<div class=\"page-break\"></div>\n");
                }

            } catch (Exception e) {
                log.error("Failed to generate label for meal item: {}", mealItemId, e);
            }
        }

        batchHtml.append("</body></html>");

        log.info("Batch labels generated successfully");

        return batchHtml.toString();
    }

    /**
     * Extract body content from HTML
     */
    private String extractBodyContent(String html) {
        int bodyStart = html.indexOf("<body>");
        int bodyEnd = html.indexOf("</body>");

        if (bodyStart != -1 && bodyEnd != -1) {
            return html.substring(bodyStart + 6, bodyEnd);
        }

        return html;
    }

    /**
     * Generate simple text label (for non-HTML printers)
     */
    @Transactional(readOnly = true)
    public String generateTextLabel(UUID mealItemId) {
        log.debug("Generating text label for meal item: {}", mealItemId);

        MealOrderItem item = mealOrderItemRepository.findById(mealItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Meal Item", "id", mealItemId));

        String processedTime = item.getProcessedAt() != null
                ? item.getProcessedAt().format(TIME_FORMATTER)
                : LocalDateTime.now().format(TIME_FORMATTER);

        return String.format("""
            ========================================
                  EVERCARE LEKKI MEAL SERVICE
            ========================================
            MEAL TYPE: %s
            ----------------------------------------
            UHID:      %s
            Patient:   %s
            Room:      %s
            ----------------------------------------
            FOOD: %s
            ----------------------------------------
            Status:    %s
            Time:      %s
            ========================================
            """,
                item.getMealType(),
                item.getMealOrder().getPatient().getUhid(),
                item.getMealOrder().getPatient().getName(),
                item.getMealOrder().getPatient().getRoomNumber(),
                item.getFoodItem().getName(),
                item.getMealStatus().getCode(),
                processedTime
        );
    }

    /**
     * Validate label dimensions for thermal printer
     */
    public boolean validateLabelDimensions(double widthMm, double heightMm) {
        // XPrinter label size: 51.2mm x 34.1mm (with tolerance)
        double tolerance = 0.5; // 0.5mm tolerance
        return Math.abs(widthMm - 51.2) <= tolerance && Math.abs(heightMm - 34.1) <= tolerance;
    }

    /**
     * Get print-ready HTML with printer-specific configurations
     */
    public String getPrintReadyHtml(UUID mealItemId, String printerType) {
        PrintLabelResponse label = generateMealLabel(mealItemId);

        // Add printer-specific CSS if needed
        String html = label.getLabelHtml();

        if ("xprinter".equalsIgnoreCase(printerType)) {
            // Add XPrinter-specific configurations
            html = html.replace("<head>", "<head>\n<meta name=\"printer\" content=\"xprinter\">");
        } else if ("zebra".equalsIgnoreCase(printerType)) {
            // Add Zebra-specific configurations
            html = html.replace("<head>", "<head>\n<meta name=\"printer\" content=\"zebra\">");
        } else if ("dymo".equalsIgnoreCase(printerType)) {
            // Add Dymo-specific configurations
            html = html.replace("<head>", "<head>\n<meta name=\"printer\" content=\"dymo\">");
        }

        return html;
    }
}