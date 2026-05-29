package com.hospital.meal.model.menu;

import com.hospital.meal.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "food_items", indexes = {
        @Index(name = "idx_food_menu_group", columnList = "menu_group_id"),
        @Index(name = "idx_food_meal_type", columnList = "meal_type"),
        @Index(name = "idx_food_active", columnList = "is_active"),
        @Index(name = "idx_food_group_type", columnList = "menu_group_id, meal_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_group_id", nullable = false, foreignKey = @ForeignKey(name = "fk_food_menu_group"))
    private MenuGroup menuGroup;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT", nullable = true)
    private String description;

    @Column(name = "meal_type", length = 50)
    private String mealType; // BREAKFAST, LUNCH, DINNER, EXTRA (nullable = can be for any meal)

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}