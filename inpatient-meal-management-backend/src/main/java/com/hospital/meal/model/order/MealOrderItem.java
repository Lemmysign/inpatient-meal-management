package com.hospital.meal.model.order;

import com.hospital.meal.model.base.BaseEntity;
import com.hospital.meal.model.menu.FoodItem;
import com.hospital.meal.model.status.MealStatus;
import com.hospital.meal.model.user.KitchenStaff;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "meal_order_items", indexes = {
        @Index(name = "idx_item_order", columnList = "meal_order_id"),
        @Index(name = "idx_item_food", columnList = "food_item_id"),
        @Index(name = "idx_item_status", columnList = "meal_status_id"),
        @Index(name = "idx_item_meal_type", columnList = "meal_type"),
        @Index(name = "idx_item_ordered_at", columnList = "ordered_at"),
        @Index(name = "idx_item_processed_at", columnList = "processed_at"),
        @Index(name = "idx_item_staff", columnList = "processed_by_staff_id"),
        @Index(name = "idx_item_status_type", columnList = "meal_status_id, meal_type"),
        @Index(name = "idx_item_status_ordered", columnList = "meal_status_id, ordered_at"),
        @Index(name = "idx_item_queue", columnList = "meal_status_id, meal_type, ordered_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealOrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_item_order"))
    private MealOrder mealOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_item_id", nullable = false, foreignKey = @ForeignKey(name = "fk_item_food"))
    private FoodItem foodItem;

    @Column(name = "meal_type", nullable = false, length = 50)
    private String mealType; // BREAKFAST, LUNCH, DINNER, EXTRA

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_status_id", nullable = false, foreignKey = @ForeignKey(name = "fk_item_status"))
    private MealStatus mealStatus;

    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_staff_id", foreignKey = @ForeignKey(name = "fk_item_staff"))
    private KitchenStaff processedByStaff;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L; // Optimistic locking
}