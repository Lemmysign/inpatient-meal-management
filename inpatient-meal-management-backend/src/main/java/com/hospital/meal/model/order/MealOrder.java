package com.hospital.meal.model.order;

import com.hospital.meal.model.base.BaseEntity;
import com.hospital.meal.model.user.Patient;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meal_orders",
        uniqueConstraints = {
                @UniqueConstraint(name = "patient_order_date", columnNames = {"patient_id", "order_date"}),
                @UniqueConstraint(name = "idempotency_key", columnNames = {"idempotency_key"})
        },
        indexes = {
                @Index(name = "idx_order_patient", columnList = "patient_id"),
                @Index(name = "idx_order_date", columnList = "order_date"),
                @Index(name = "idx_order_patient_date", columnList = "patient_id, order_date"),
                @Index(name = "idx_order_idempotency", columnList = "idempotency_key"),
                @Index(name = "idx_order_uhid", columnList = "uhid") // ✅ ADD INDEX for UHID
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealOrder extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false, foreignKey = @ForeignKey(name = "fk_meal_order_patient"))
    private Patient patient;

    // ✅ ADD THIS: Store patient UHID directly
    @Column(name = "uhid", nullable = false, length = 50)
    private String uhid;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "idempotency_key", unique = true, length = 255)
    private String idempotencyKey;

    @OneToMany(mappedBy = "mealOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MealOrderItem> items = new ArrayList<>();
}