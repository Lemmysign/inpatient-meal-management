package com.hospital.meal.model.menu;

import com.hospital.meal.model.base.BaseEntity;
import com.hospital.meal.model.user.Dietician;
import com.hospital.meal.model.user.Patient;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_menus",
        uniqueConstraints = {
                @UniqueConstraint(name = "patient_menu_date", columnNames = {"patient_id", "menu_group_id", "valid_from"})
        },
        indexes = {
                @Index(name = "idx_patient_menu_patient", columnList = "patient_id"),
                @Index(name = "idx_patient_menu_group", columnList = "menu_group_id"),
                @Index(name = "idx_patient_menu_dietician", columnList = "assigned_by_dietician_id"),
                @Index(name = "idx_patient_menu_valid_from", columnList = "valid_from"),
                @Index(name = "idx_patient_menu_valid_until", columnList = "valid_until"),
                @Index(name = "idx_patient_menu_active", columnList = "is_active"),
                @Index(name = "idx_patient_menu_validity", columnList = "patient_id, valid_from, valid_until, is_active"),
                @Index(name = "idx_patient_menu_uhid", columnList = "uhid") // ✅ ADD INDEX for UHID
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientMenu extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false, foreignKey = @ForeignKey(name = "fk_patient_menu_patient"))
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_group_id", nullable = false, foreignKey = @ForeignKey(name = "fk_patient_menu_group"))
    private MenuGroup menuGroup;

    // ✅ Store menu group name directly
    @Column(name = "menu_group_name", nullable = false, length = 255)
    private String menuGroupName;

    // ✅ ADD THIS: Store patient UHID directly
    @Column(name = "uhid", nullable = false, length = 50)
    private String uhid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_dietician_id", nullable = false, foreignKey = @ForeignKey(name = "fk_patient_menu_dietician"))
    private Dietician assignedByDietician;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}