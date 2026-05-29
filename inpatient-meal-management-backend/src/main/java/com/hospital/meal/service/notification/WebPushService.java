package com.hospital.meal.service.notification;

import com.hospital.meal.model.notification.WebPushSubscription;

import java.util.UUID;

public interface WebPushService {

    /**
     * Subscribe user to web push notifications
     */
    WebPushSubscription subscribe(String userType, UUID userId, String endpoint, String p256dhKey, String authKey);


    /**
     * Get VAPID public key
     * @return Public key string
     */
    String getPublicKey();

    /**
     * Unsubscribe from web push notifications
     */
    void unsubscribe(String endpoint);

    /**
     * Send push notification to specific user
     */
    void sendNotification(String userType, UUID userId, String title, String body);

    /**
     * Send push notification to all users of a type (e.g., all kitchen staff)
     */
    void sendNotificationToUserType(String userType, String title, String body);

    /**
     * Get subscription count for a specific user
     */
    int getSubscriptionCount(String userType, UUID userId);
}