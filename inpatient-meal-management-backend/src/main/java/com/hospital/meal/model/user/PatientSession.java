package com.hospital.meal.model.user;

import com.hospital.meal.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "patient_sessions", indexes = {
        @Index(name = "idx_session_token", columnList = "session_token", unique = true),
        @Index(name = "idx_session_patient", columnList = "patient_id"),
        @Index(name = "idx_session_expires", columnList = "expires_at"),
        @Index(name = "idx_session_active", columnList = "is_active"),
        @Index(name = "idx_session_patient_active", columnList = "patient_id, is_active"),
        @Index(name = "idx_session_uhid", columnList = "uhid") // ✅ ADD INDEX for UHID
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false, foreignKey = @ForeignKey(name = "fk_session_patient"))
    private Patient patient;

    // ✅ ADD THIS: Store patient UHID directly
    @Column(name = "uhid", nullable = false, length = 50)
    private String uhid;

    @Column(name = "session_token", nullable = false, unique = true, length = 500)
    private String sessionToken;

    // ✅ KEEP: For audit trail
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    // ✅ KEEP: For troubleshooting
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "last_activity_at", nullable = false)
    private LocalDateTime lastActivityAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}