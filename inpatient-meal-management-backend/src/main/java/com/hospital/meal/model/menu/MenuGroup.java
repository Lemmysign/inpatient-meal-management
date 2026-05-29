package com.hospital.meal.model.menu;

import com.hospital.meal.model.base.BaseEntity;
import com.hospital.meal.model.user.Dietician;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menu_groups", indexes = {
        @Index(name = "idx_menu_group_name", columnList = "name", unique = true),
        @Index(name = "idx_menu_group_predefined", columnList = "is_predefined"),
        @Index(name = "idx_menu_group_active", columnList = "is_active"),
        @Index(name = "idx_menu_group_dietician", columnList = "created_by_dietician_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuGroup extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_predefined", nullable = false)
    private Boolean isPredefined = false;

    @Column(name = "is_alacarte")
    private Boolean isAlacarte = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_dietician_id", foreignKey = @ForeignKey(name = "fk_menu_group_dietician"))
    private Dietician createdByDietician;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;


}