package com.hospital.meal.service.service_impl.audit;

import com.hospital.meal.model.audit.AuditLog;
import com.hospital.meal.repository.AuditLogRepository;
import com.hospital.meal.service.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String userType, UUID userId, String userName, String action,
                    String entityType, UUID entityId, String details, String ipAddress) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userType(userType)
                    .userId(userId)
                    .userName(userName)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(details)
                    .ipAddress(ipAddress)
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);

            log.debug("Audit log created: {} by {} ({})", action, userName, userType);
        } catch (Exception e) {
            // Never let audit logging break the main transaction
            log.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLogin(String userType, UUID userId, String userName, String ipAddress) {
        log(userType, userId, userName, "LOGIN",
                userType, userId,
                String.format("User %s logged in", userName),
                ipAddress);
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logMealOrderCreated(UUID patientId, String patientName, UUID orderId, String ipAddress) {
        log("PATIENT", patientId, patientName, "CREATE_MEAL_ORDER",
                "MealOrder", orderId,
                String.format("Patient %s created meal order", patientName),
                ipAddress);
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logMealProcessed(UUID staffId, String staffName, UUID mealItemId, String mealType, String ipAddress) {
        log("KITCHEN_STAFF", staffId, staffName, "PROCESS_MEAL",
                "MealOrderItem", mealItemId,
                String.format("Kitchen staff %s processed %s meal", staffName, mealType),
                ipAddress);
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logMenuAssigned(UUID dieticianId, String dieticianName, UUID patientId, String uhid, String ipAddress) {
        log("DIETICIAN", dieticianId, dieticianName, "ASSIGN_MENU",
                "PatientMenu", patientId,
                String.format("Dietician %s assigned menu to patient %s", dieticianName, uhid),
                ipAddress);
    }

    /**
     * Additional audit logging methods for comprehensive tracking
     */

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logMenuGroupCreated(UUID dieticianId, String dieticianName, UUID menuGroupId, String menuGroupName, String ipAddress) {
        log("DIETICIAN", dieticianId, dieticianName, "CREATE_MENU_GROUP",
                "MenuGroup", menuGroupId,
                String.format("Created menu group: %s", menuGroupName),
                ipAddress);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logMenuGroupUpdated(UUID dieticianId, String dieticianName, UUID menuGroupId, String menuGroupName, String ipAddress) {
        log("DIETICIAN", dieticianId, dieticianName, "UPDATE_MENU_GROUP",
                "MenuGroup", menuGroupId,
                String.format("Updated menu group: %s", menuGroupName),
                ipAddress);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logMenuGroupDeleted(UUID dieticianId, String dieticianName, UUID menuGroupId, String menuGroupName, String ipAddress) {
        log("DIETICIAN", dieticianId, dieticianName, "DELETE_MENU_GROUP",
                "MenuGroup", menuGroupId,
                String.format("Deleted menu group: %s", menuGroupName),
                ipAddress);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFoodItemCreated(UUID dieticianId, String dieticianName, UUID foodItemId, String foodItemName, String ipAddress) {
        log("DIETICIAN", dieticianId, dieticianName, "CREATE_FOOD_ITEM",
                "FoodItem", foodItemId,
                String.format("Created food item: %s", foodItemName),
                ipAddress);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFoodItemUpdated(UUID dieticianId, String dieticianName, UUID foodItemId, String foodItemName, String ipAddress) {
        log("DIETICIAN", dieticianId, dieticianName, "UPDATE_FOOD_ITEM",
                "FoodItem", foodItemId,
                String.format("Updated food item: %s", foodItemName),
                ipAddress);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFoodItemDeleted(UUID dieticianId, String dieticianName, UUID foodItemId, String foodItemName, String ipAddress) {
        log("DIETICIAN", dieticianId, dieticianName, "DELETE_FOOD_ITEM",
                "FoodItem", foodItemId,
                String.format("Deleted food item: %s", foodItemName),
                ipAddress);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logMealOrderModified(UUID patientId, String patientName, UUID orderItemId, String mealType,
                                     String oldFood, String newFood, String ipAddress) {
        log("PATIENT", patientId, patientName, "MODIFY_MEAL_ORDER",
                "MealOrderItem", orderItemId,
                String.format("Modified %s: %s → %s", mealType, oldFood, newFood),
                ipAddress);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logDieticianCreated(UUID adminId, String adminName, UUID dieticianId, String dieticianEmail, String ipAddress) {
        log("ADMIN", adminId, adminName, "CREATE_DIETICIAN",
                "Dietician", dieticianId,
                String.format("Created dietician account: %s", dieticianEmail),
                ipAddress);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logKitchenStaffCreated(UUID adminId, String adminName, UUID staffId, String staffEmail, String ipAddress) {
        log("ADMIN", adminId, adminName, "CREATE_KITCHEN_STAFF",
                "KitchenStaff", staffId,
                String.format("Created kitchen staff account: %s", staffEmail),
                ipAddress);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logUserStatusToggled(UUID adminId, String adminName, UUID userId, String userType, boolean isActive, String ipAddress) {
        String action = isActive ? "ENABLE_USER" : "DISABLE_USER";
        log("ADMIN", adminId, adminName, action,
                userType, userId,
                String.format("%s %s account", isActive ? "Enabled" : "Disabled", userType),
                ipAddress);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logPasswordSet(UUID dieticianId, String dieticianEmail, String ipAddress) {
        log("DIETICIAN", dieticianId, dieticianEmail, "SET_PASSWORD",
                "Dietician", dieticianId,
                "Password set via invitation link",
                ipAddress);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logInviteResent(UUID adminId, String adminName, UUID dieticianId, String dieticianEmail, String ipAddress) {
        log("ADMIN", adminId, adminName, "RESEND_INVITE",
                "DieticianInvite", dieticianId,
                String.format("Resent invitation to: %s", dieticianEmail),
                ipAddress);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSessionCreated(UUID patientId, String patientName, String uhid, String ipAddress) {
        log("PATIENT", patientId, patientName, "CREATE_SESSION",
                "PatientSession", patientId,
                String.format("Patient session created for UHID: %s", uhid),
                ipAddress);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSessionExpired(UUID patientId, String patientName, String ipAddress) {
        log("PATIENT", patientId, patientName, "SESSION_EXPIRED",
                "PatientSession", patientId,
                "Patient session expired",
                ipAddress);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logLogout(String userType, UUID userId, String userName, String ipAddress) {
        log(userType, userId, userName, "LOGOUT",
                userType, userId,
                String.format("User %s logged out", userName),
                ipAddress);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logUnauthorizedAccess(String attemptedAction, String ipAddress, String details) {
        log("SYSTEM", null, "UNAUTHORIZED", "UNAUTHORIZED_ACCESS",
                "Security", null,
                String.format("Unauthorized access attempt: %s - %s", attemptedAction, details),
                ipAddress);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logValidationError(String userType, UUID userId, String userName, String action, String errorDetails, String ipAddress) {
        log(userType, userId, userName, "VALIDATION_ERROR",
                action, null,
                String.format("Validation failed: %s", errorDetails),
                ipAddress);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logConcurrencyConflict(UUID staffId, String staffName, UUID mealItemId, String ipAddress) {
        log("KITCHEN_STAFF", staffId, staffName, "CONCURRENCY_CONFLICT",
                "MealOrderItem", mealItemId,
                "Attempted to process meal already being processed by another staff member",
                ipAddress);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logPatientMenuDeactivated(UUID dieticianId, String dieticianName, UUID patientMenuId, String uhid, String ipAddress) {
        log("DIETICIAN", dieticianId, dieticianName, "DEACTIVATE_PATIENT_MENU",
                "PatientMenu", patientMenuId,
                String.format("Deactivated menu for patient: %s", uhid),
                ipAddress);
    }
}