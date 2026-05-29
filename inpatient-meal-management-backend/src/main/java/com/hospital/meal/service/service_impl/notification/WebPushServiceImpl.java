package com.hospital.meal.service.service_impl.notification;

import com.hospital.meal.model.notification.WebPushSubscription;
import com.hospital.meal.repository.WebPushSubscriptionRepository;
import com.hospital.meal.service.notification.WebPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Utils;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebPushServiceImpl implements WebPushService {

    private final WebPushSubscriptionRepository webPushSubscriptionRepository;

    @Value("${webpush.public.key:}")
    private String publicKey;

    @Value("${webpush.private.key:}")
    private String privateKey;

    @Value("${webpush.subject:mailto:admin@hospital.com}")
    private String subject;

    @Value("${webpush.enabled:false}")
    private boolean webPushEnabled;



    private PushService pushService;

    @PostConstruct
    public void init() {
        if (!webPushEnabled) {
            log.warn("Web Push service is disabled");
            return;
        }

        try {
            // Add BouncyCastle as security provider
            Security.addProvider(new BouncyCastleProvider());

            // Initialize push service
            if (publicKey == null || publicKey.isEmpty() || privateKey == null || privateKey.isEmpty()) {
                log.warn("Web Push keys not configured. Generating new keys...");
                // In production, these should be pre-generated and stored securely
                // For now, we'll just log a warning
                log.error("Please configure webpush.public.key and webpush.private.key in application properties");
            } else {
                pushService = new PushService(publicKey, privateKey, subject);
                log.info("Web Push service initialized successfully");
            }
        } catch (GeneralSecurityException e) {
            log.error("Failed to initialize Web Push service", e);
        }
    }


    @Override
    public String getPublicKey() {
        return publicKey;
    }

    @Override
    @Transactional
    public WebPushSubscription subscribe(String userType, UUID userId, String endpoint,
                                         String p256dhKey, String authKey) {
        log.info("Subscribing user to web push: {} - {}", userType, userId);

        // Delete existing subscription for this endpoint first and flush immediately
        webPushSubscriptionRepository.findByEndpoint(endpoint).ifPresent(existing -> {
            log.info("Removing existing subscription for endpoint");
            webPushSubscriptionRepository.delete(existing);
            webPushSubscriptionRepository.flush(); // ← force delete to execute before insert
        });

        // Create new subscription
        WebPushSubscription subscription = WebPushSubscription.builder()
                .userType(userType)
                .userId(userId)
                .endpoint(endpoint)
                .p256dhKey(p256dhKey)
                .authKey(authKey)
                .build();

        subscription = webPushSubscriptionRepository.save(subscription);

        log.info("Web push subscription saved successfully");

        return subscription;
    }

    @Override
    @Transactional
    public void unsubscribe(String endpoint) {
        log.info("Unsubscribing from web push: {}", endpoint);

        webPushSubscriptionRepository.findByEndpoint(endpoint).ifPresent(subscription -> {
            webPushSubscriptionRepository.delete(subscription);
            log.info("Web push subscription removed");
        });
    }

    @Override
    @Async
    public void sendNotification(String userType, UUID userId, String title, String body) {
        log.info("Sending web push notification to user: {} - {}", userType, userId);

        if (!webPushEnabled || pushService == null) {
            log.warn("Web Push service is not enabled or not initialized. Skipping notification.");
            return;
        }

        // Get all subscriptions for this user
        List<WebPushSubscription> subscriptions = webPushSubscriptionRepository
                .findByUserTypeAndUserId(userType, userId);

        if (subscriptions.isEmpty()) {
            log.debug("No web push subscriptions found for user: {} - {}", userType, userId);
            return;
        }

        // Build notification payload
        String payload = buildNotificationPayload(title, body);

        // Send to all subscriptions
        for (WebPushSubscription subscription : subscriptions) {
            try {
                sendToSubscription(subscription, payload);
            } catch (Exception e) {
                log.error("Failed to send notification to subscription: {}", subscription.getEndpoint(), e);
                // Consider removing invalid subscriptions
                handleFailedSubscription(subscription, e);
            }
        }
    }

    @Override
    @Async
    public void sendNotificationToUserType(String userType, String title, String body) {
        log.info("Sending web push notification to all users of type: {}", userType);

        if (!webPushEnabled || pushService == null) {
            log.warn("Web Push service is not enabled or not initialized. Skipping notification.");
            return;
        }

        // Get all subscriptions for this user type
        List<WebPushSubscription> subscriptions = webPushSubscriptionRepository
                .findByUserType(userType);

        if (subscriptions.isEmpty()) {
            log.debug("No web push subscriptions found for user type: {}", userType);
            return;
        }

        log.info("Sending notification to {} subscriptions", subscriptions.size());

        // Build notification payload
        String payload = buildNotificationPayload(title, body);

        // Send to all subscriptions
        for (WebPushSubscription subscription : subscriptions) {
            try {
                sendToSubscription(subscription, payload);
            } catch (Exception e) {
                log.error("Failed to send notification to subscription: {}", subscription.getEndpoint(), e);
                handleFailedSubscription(subscription, e);
            }
        }
    }

    /**
     * Send notification to a specific subscription
     */
    private void sendToSubscription(WebPushSubscription subscription, String payload)
            throws GeneralSecurityException, InterruptedException, ExecutionException, JoseException, IOException {

        // Create notification
        Notification notification = new Notification(
                subscription.getEndpoint(),
                subscription.getP256dhKey(),
                subscription.getAuthKey(),
                payload
        );

        // Send notification
        HttpResponse response = pushService.send(notification);

        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == 201) {
            log.debug("Notification sent successfully to: {}", subscription.getEndpoint());
        } else if (statusCode == 410) {
            // Subscription expired or invalid
            log.warn("Subscription expired or invalid: {}", subscription.getEndpoint());
            webPushSubscriptionRepository.delete(subscription);
        } else {
            log.warn("Unexpected response code: {} for endpoint: {}", statusCode, subscription.getEndpoint());
        }
    }

    /**
     * Handle failed subscription delivery
     */
    private void handleFailedSubscription(WebPushSubscription subscription, Exception error) {
        String errorMessage = error.getMessage();

        // Remove subscription if it's permanently invalid
        if (errorMessage != null && (
                errorMessage.contains("410") ||
                        errorMessage.contains("404") ||
                        errorMessage.contains("invalid"))) {

            log.info("Removing invalid subscription: {}", subscription.getEndpoint());
            webPushSubscriptionRepository.delete(subscription);
        }
    }

    /**
     * Build notification payload in JSON format
     */
    private String buildNotificationPayload(String title, String body) {
        return String.format("""
            {
                "title": "%s",
                "body": "%s",
                "icon": "/icon.png",
                "badge": "/badge.png",
                "vibrate": [200, 100, 200],
                "tag": "meal-notification",
                "requireInteraction": false
            }
            """, escapeJson(title), escapeJson(body));
    }

    /**
     * Escape JSON special characters
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Send custom notification with additional data
     */
    @Async
    public void sendCustomNotification(String userType, UUID userId, String title,
                                       String body, String icon, String url) {
        log.info("Sending custom web push notification to user: {} - {}", userType, userId);

        if (!webPushEnabled || pushService == null) {
            log.warn("Web Push service is not enabled. Skipping notification.");
            return;
        }

        List<WebPushSubscription> subscriptions = webPushSubscriptionRepository
                .findByUserTypeAndUserId(userType, userId);

        if (subscriptions.isEmpty()) {
            return;
        }

        String payload = buildCustomNotificationPayload(title, body, icon, url);

        for (WebPushSubscription subscription : subscriptions) {
            try {
                sendToSubscription(subscription, payload);
            } catch (Exception e) {
                log.error("Failed to send custom notification", e);
                handleFailedSubscription(subscription, e);
            }
        }
    }

    /**
     * Build custom notification payload
     */
    private String buildCustomNotificationPayload(String title, String body, String icon, String url) {
        return String.format("""
            {
                "title": "%s",
                "body": "%s",
                "icon": "%s",
                "badge": "/badge.png",
                "vibrate": [200, 100, 200],
                "tag": "meal-notification",
                "requireInteraction": true,
                "data": {
                    "url": "%s"
                }
            }
            """, escapeJson(title), escapeJson(body), escapeJson(icon), escapeJson(url));
    }

    /**
     * Get subscription count for a user
     */
    @Transactional(readOnly = true)
    public int getSubscriptionCount(String userType, UUID userId) {
        return webPushSubscriptionRepository.findByUserTypeAndUserId(userType, userId).size();
    }

    /**
     * Get total subscription count for a user type
     */
    @Transactional(readOnly = true)
    public int getSubscriptionCountByType(String userType) {
        return webPushSubscriptionRepository.findByUserType(userType).size();
    }
}