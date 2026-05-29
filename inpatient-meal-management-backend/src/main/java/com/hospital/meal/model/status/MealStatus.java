package com.hospital.meal.model.status;

import com.hospital.meal.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "meal_statuses", indexes = {
        @Index(name = "idx_status_code", columnList = "code", unique = true),
        @Index(name = "idx_status_sort", columnList = "sort_order")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealStatus extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code; // PENDING, PROCESSING, PROCESSED

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "sort_order")
    private Integer sortOrder;
}