package com.hospital.meal.model.notification;

import com.hospital.meal.model.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "web_push_subscriptions", indexes = {
        @Index(name = "idx_push_endpoint", columnList = "endpoint", unique = true),
        @Index(name = "idx_push_user", columnList = "user_type, user_id"),
        @Index(name = "idx_push_user_type", columnList = "user_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebPushSubscription extends BaseEntity {

    @Column(name = "user_type", nullable = false, length = 50)
    private String userType; // DIETICIAN, KITCHEN_STAFF

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "endpoint", nullable = false, unique = true, columnDefinition = "TEXT")
    private String endpoint;

    @Column(name = "p256dh_key", nullable = false, columnDefinition = "TEXT")
    private String p256dhKey;

    @Column(name = "auth_key", nullable = false, columnDefinition = "TEXT")
    private String authKey;
}