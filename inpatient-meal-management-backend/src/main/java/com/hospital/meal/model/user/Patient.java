package com.hospital.meal.model.user;

import com.hospital.meal.model.base.BaseEntity;
import com.hospital.meal.model.menu.PatientMenu;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "patients", indexes = {
        @Index(name = "idx_patient_uhid", columnList = "uhid", unique = true),
        @Index(name = "idx_patient_name", columnList = "name"),
        @Index(name = "idx_patient_room", columnList = "room_number"),
        @Index(name = "idx_patient_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient extends BaseEntity {

    @Column(name = "uhid", nullable = false, unique = true, length = 50)
    private String uhid;

    @Column(name = "name", nullable = true, length = 255)
    private String name;

    @Column(name = "room_number", nullable = true, length = 50)
    private String roomNumber;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "patient", fetch = FetchType.LAZY)
    @BatchSize(size = 25)
    private List<PatientMenu> patientMenus = new ArrayList<>();
}