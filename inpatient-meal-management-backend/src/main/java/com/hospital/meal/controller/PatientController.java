package com.hospital.meal.controller;

import com.hospital.meal.constant.ApiConstants;
import com.hospital.meal.dto.common.ApiResponse;
import com.hospital.meal.dto.patient.*;
import com.hospital.meal.exception.InvalidRequestException;
import com.hospital.meal.exception.ResourceNotFoundException;
import com.hospital.meal.model.user.PatientSession;
import com.hospital.meal.repository.PatientSessionRepository;
import com.hospital.meal.security.userdetails.UserPrincipal;
import com.hospital.meal.service.patient.MealOrderService;
import com.hospital.meal.util.DateUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.PATIENT_BASE)
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Patient", description = "Patient meal ordering endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PatientController {

    private final MealOrderService mealOrderService;
    private final PatientSessionRepository patientSessionRepository;

    /**
     * Get available menu for patient
     */
    @GetMapping(ApiConstants.PATIENT_MENU)
    @Operation(summary = "Get Available Menu", description = "Get available menu options for the current patient")
    public ResponseEntity<ApiResponse<AvailableMenuResponse>> getAvailableMenu(
            @RequestParam(required = false) String date,
            @RequestAttribute("uhid") String uhid) {

        log.info("Get available menu request for UHID: {}", uhid);

        LocalDate targetDate = date != null ? LocalDate.parse(date) : DateUtil.getCurrentDate();

        AvailableMenuResponse menu = mealOrderService.getAvailableMenu(uhid, targetDate);

        log.info("Available menu retrieved successfully for UHID: {}", uhid);

        return ResponseEntity.ok(
                ApiResponse.success("Menu retrieved successfully", menu)
        );
    }

    /**
     * Create meal order for today
     */
    @PostMapping(ApiConstants.PATIENT_ORDERS)
    @Operation(summary = "Create Meal Order", description = "Create meal order for today (must select breakfast, lunch, and dinner)")
    public ResponseEntity<ApiResponse<MealOrderResponse>> createMealOrder(
            @Valid @RequestBody CreateMealOrderRequest request,
            @RequestAttribute("uhid") String uhid) {

        log.info("Create meal order request for UHID: {}", uhid);

        MealOrderResponse order = mealOrderService.createMealOrder(uhid, request);

        log.info("Meal order created successfully for UHID: {}", uhid);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Meal order created successfully", order));
    }

    /**
     * Get today's meal order
     */
    @GetMapping(ApiConstants.PATIENT_ORDERS + "/today")
    @Operation(summary = "Get Today's Order", description = "Get meal order for today")
    public ResponseEntity<ApiResponse<MealOrderResponse>> getTodaysOrder(
            @RequestAttribute("uhid") String uhid) {

        log.info("Get today's order request for UHID: {}", uhid);

        MealOrderResponse order = mealOrderService.getTodaysMealOrder(uhid);

        log.info("Today's order retrieved successfully for UHID: {}", uhid);

        return ResponseEntity.ok(
                ApiResponse.success("Order retrieved successfully", order)
        );
    }

    /**
     * Get meal order history
     */
    @GetMapping(ApiConstants.PATIENT_ORDER_HISTORY)
    @Operation(summary = "Get Order History", description = "Get all past meal orders for the patient")
    public ResponseEntity<ApiResponse<List<MealOrderResponse>>> getOrderHistory(
            @RequestAttribute("uhid") String uhid) {

        log.info("Get order history request for UHID: {}", uhid);

        List<MealOrderResponse> history = mealOrderService.getMealOrderHistory(uhid);

        log.info("Order history retrieved successfully for UHID: {}", uhid);

        return ResponseEntity.ok(
                ApiResponse.success("Order history retrieved successfully", history)
        );
    }

    /**
     * Modify specific meal in today's order
     */
    @PutMapping(ApiConstants.PATIENT_ORDERS + "/{mealType}")
    @Operation(summary = "Modify Meal", description = "Modify a specific meal in today's order (if within cutoff time and not yet processed)")
    public ResponseEntity<ApiResponse<MealOrderResponse>> modifyMeal(
            @PathVariable String mealType,
            @Valid @RequestBody UpdateMealOrderRequest request,
            @RequestAttribute("uhid") String uhid) {

        log.info("Modify meal request for UHID: {}, meal type: {}", uhid, mealType);

        MealOrderResponse order = mealOrderService.modifyMeal(
                uhid,
                mealType.toUpperCase(),
                request.getNewFoodItemId(),
                request.getReason()
        );

        log.info("Meal modified successfully for UHID: {}", uhid);

        return ResponseEntity.ok(
                ApiResponse.success("Meal modified successfully", order)
        );
    }

    /**
     * Check if meal can be modified
     */
    @GetMapping(ApiConstants.PATIENT_ORDERS + "/can-modify/{mealType}")
    @Operation(summary = "Check Modification Allowed", description = "Check if a specific meal type can still be modified")
    public ResponseEntity<ApiResponse<Boolean>> canModifyMeal(
            @PathVariable String mealType,
            @RequestAttribute("uhid") String uhid) {

        log.debug("Check can modify request for UHID: {}, meal type: {}", uhid, mealType);

        boolean canModify = mealOrderService.canModifyMeal(uhid, mealType.toUpperCase());

        return ResponseEntity.ok(
                ApiResponse.success("Modification check result", canModify)
        );
    }

    /**
     * Get patient profile
     */
    @GetMapping("/profile")
    @Operation(summary = "Get Profile", description = "Get current patient profile information")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> getProfile(
            @RequestAttribute("uhid") String uhid,
            @RequestAttribute("patientName") String name,
            @RequestAttribute("roomNumber") String roomNumber) {

        log.debug("Get profile request for UHID: {}", uhid);

        PatientProfileResponse profile = PatientProfileResponse.builder()
                .uhid(uhid)
                .name(name)
                .roomNumber(roomNumber)
                .build();

        return ResponseEntity.ok(
                ApiResponse.success("Profile retrieved successfully", profile)
        );
    }


    /**
     * Get à la carte menu items (available to all patients)
     */
    @GetMapping("/alacarte")
    @Operation(
            summary = "Get à la carte menu",
            description = "Get special items available outside your diet plan - order anytime!"
    )
    public ResponseEntity<ApiResponse<List<AvailableMenuResponse.FoodOption>>> getAlacarteMenu(
            @RequestHeader(value = "X-Patient-Session", required = false) String sessionToken  // ✅ Optional
    ) {
        log.info("Getting à la carte menu");

        List<AvailableMenuResponse.FoodOption> items = mealOrderService.getAlacarteMenu();

        return ResponseEntity.ok(
                ApiResponse.success("À la carte menu retrieved successfully", items)
        );
    }


    /**
     * Create à la carte order (no time or meal restrictions)
     */
    /**
     * Create à la carte order (no time or meal restrictions)
     */
    @PostMapping("/orders/alacarte")
    @Operation(
            summary = "Order à la carte items",
            description = "Order special items anytime - no time restrictions or meal requirements"
    )
    public ResponseEntity<ApiResponse<MealOrderResponse>> createAlacarteOrder(
            @Valid @RequestBody AlacarteOrderRequest request,
            @RequestHeader(value = "X-Patient-Session", required = false) String sessionToken,  // ← Make optional
            @RequestHeader(value = "Authorization", required = false) String authHeader  // ← Add this
    ) {
        log.info("Creating à la carte order with {} items", request.getFoodItemIds().size());

        // Extract token from either header
        String token = sessionToken;
        if (token == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token == null) {
            throw new InvalidRequestException("Authentication required. Provide X-Patient-Session or Authorization header.");
        }

        String uhid = extractUhidFromSession(token);

        MealOrderResponse response = mealOrderService.createAlacarteOrder(uhid, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("À la carte order created successfully", response));
    }

    /**
     * Extract UHID from patient session token
     *
     * @param sessionToken Patient session token from X-Patient-Session header
     * @return Patient UHID
     * @throws ResourceNotFoundException if session token is invalid or expired
     */
    private String extractUhidFromSession(String sessionToken) {
        log.debug("Extracting UHID from session token");

        PatientSession session = patientSessionRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Session", "token", sessionToken));

        if (!session.getIsActive()) {
            throw new InvalidRequestException("Session has been logged out");
        }

        return session.getPatient().getUhid();
    }




    /**
     * Check if patient has active menu
     */
    @GetMapping("/has-menu")
    @Operation(summary = "Check Active Menu", description = "Check if patient has an active menu assigned")
    public ResponseEntity<ApiResponse<Boolean>> hasActiveMenu(
            @RequestAttribute("uhid") String uhid) {

        log.debug("Check active menu for UHID: {}", uhid);

        try {
            mealOrderService.getAvailableMenu(uhid, DateUtil.getCurrentDate());
            return ResponseEntity.ok(
                    ApiResponse.success("Menu status", true)
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    ApiResponse.success("Menu status", false)
            );
        }
    }

    /**
     * Inner class for patient profile response
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PatientProfileResponse {
        private String uhid;
        private String name;
        private String roomNumber;
    }
}