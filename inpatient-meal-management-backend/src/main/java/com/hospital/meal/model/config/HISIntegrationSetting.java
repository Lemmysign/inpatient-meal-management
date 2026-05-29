package com.hospital.meal.model.config;

import jakarta.persistence.*;
import lombok.*;

/**
 * Stores the runtime HIS integration toggle in the database.
 * This allows switching between HIS and Manual mode
 * without restarting the application.
 *
 * Only one row ever exists (id = 1).
 */
@Entity
@Table(name = "his_integration_setting")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HISIntegrationSetting {

    @Id
    @Column(name = "id")
    private Long id = 1L;

    /**
     * true  = use HIS scraper
     * false = use manual entry
     */
    @Column(name = "enabled", nullable = false)
    private boolean enabled = false;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;
}