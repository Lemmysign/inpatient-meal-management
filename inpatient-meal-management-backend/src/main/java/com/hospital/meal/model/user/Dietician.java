package com.hospital.meal.model.user;

import com.hospital.meal.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dieticians", indexes = {
        @Index(name = "idx_dietician_email", columnList = "email"),
        @Index(name = "idx_dietician_staff_id", columnList = "staff_id"),
        @Index(name = "idx_dietician_name", columnList = "name"),
        @Index(name = "idx_dietician_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dietician extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "staff_id", length = 50)
    private String staffId;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}