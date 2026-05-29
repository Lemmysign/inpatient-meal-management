package com.hospital.meal.model.user;

import com.hospital.meal.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dietician_invites", indexes = {
        @Index(name = "idx_invite_token", columnList = "token"),
        @Index(name = "idx_invite_dietician", columnList = "dietician_id"),
        @Index(name = "idx_invite_expires", columnList = "expires_at"),
        @Index(name = "idx_invite_used", columnList = "used_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DieticianInvite extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dietician_id", nullable = false, foreignKey = @ForeignKey(name = "fk_invite_dietician"))
    private Dietician dietician;

    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;
}