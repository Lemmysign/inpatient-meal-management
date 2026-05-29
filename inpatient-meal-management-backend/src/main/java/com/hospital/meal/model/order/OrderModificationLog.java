package com.hospital.meal.model.order;

import com.hospital.meal.model.base.BaseEntity;
import com.hospital.meal.model.menu.FoodItem;
import com.hospital.meal.model.user.Patient;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_modification_logs", indexes = {
        @Index(name = "idx_mod_patient", columnList = "patient_id"),
        @Index(name = "idx_mod_order", columnList = "meal_order_id"),
        @Index(name = "idx_mod_order_item", columnList = "meal_order_item_id"),
        @Index(name = "idx_mod_meal_type", columnList = "meal_type"),
        @Index(name = "idx_mod_timestamp", columnList = "modified_at"),
        @Index(name = "idx_mod_patient_date", columnList = "patient_id, modified_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderModificationLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mod_patient"))
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mod_order"))
    private MealOrder mealOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_order_item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mod_order_item"))
    private MealOrderItem mealOrderItem;

    @Column(name = "meal_type", nullable = false, length = 50)
    private String mealType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_food_item_id", foreignKey = @ForeignKey(name = "fk_mod_old_food"))
    private FoodItem oldFoodItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_food_item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mod_new_food"))
    private FoodItem newFoodItem;

    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    @Column(name = "modification_reason", columnDefinition = "TEXT")
    private String modificationReason;
}