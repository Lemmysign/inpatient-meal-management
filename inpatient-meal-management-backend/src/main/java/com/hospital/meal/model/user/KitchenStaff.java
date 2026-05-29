package com.hospital.meal.model.user;

import com.hospital.meal.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kitchen_staff", indexes = {
        @Index(name = "idx_kitchen_email", columnList = "email"),
        @Index(name = "idx_kitchen_name", columnList = "name"),
        @Index(name = "idx_kitchen_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitchenStaff extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "session_duration_hours", nullable = false)
    private Integer sessionDurationHours = 24;
}