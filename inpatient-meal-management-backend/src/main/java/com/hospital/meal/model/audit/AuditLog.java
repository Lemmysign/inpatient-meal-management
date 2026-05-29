package com.hospital.meal.model.audit;

import com.hospital.meal.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_user", columnList = "user_type, user_id"),
        @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
        @Index(name = "idx_audit_user_timestamp", columnList = "user_type, user_id, timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

    @Column(name = "user_type", length = 50)
    private String userType; // ADMIN, DIETICIAN, KITCHEN_STAFF, PATIENT, SYSTEM

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "user_name", length = 255)
    private String userName;

    @Column(name = "action", nullable = false, length = 100)
    private String action; // LOGIN, CREATE_MEAL_ORDER, PROCESS_MEAL, ASSIGN_MENU, etc.

    @Column(name = "entity_type", length = 100)
    private String entityType; // MEAL_ORDER, MENU_GROUP, PATIENT, etc.

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details; // Human-readable description

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}