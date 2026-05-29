package com.hospital.meal.service.audit;

import com.hospital.meal.model.audit.AuditLog;

import java.util.UUID;

public interface AuditLogService {

    /**
     * Log an audit event
     */
    void log(String userType, UUID userId, String userName, String action,
             String entityType, UUID entityId, String details, String ipAddress);

    /**
     * Log login event
     */
    void logLogin(String userType, UUID userId, String userName, String ipAddress);

    /**
     * Log meal order creation
     */
    void logMealOrderCreated(UUID patientId, String patientName, UUID orderId, String ipAddress);

    /**
     * Log meal processing
     */
    void logMealProcessed(UUID staffId, String staffName, UUID mealItemId, String mealType, String ipAddress);

    /**
     * Log menu assignment
     */
    void logMenuAssigned(UUID dieticianId, String dieticianName, UUID patientId, String uhid, String ipAddress);
}