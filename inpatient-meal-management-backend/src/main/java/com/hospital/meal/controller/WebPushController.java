package com.hospital.meal.controller;

import com.hospital.meal.constant.ApiConstants;
import com.hospital.meal.dto.common.ApiResponse;
import com.hospital.meal.model.notification.WebPushSubscription;
import com.hospital.meal.service.notification.WebPushService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.WEB_PUSH_BASE)
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Web Push Notifications", description = "Web push notification subscription management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class WebPushController {

    private final WebPushService webPushService;

    /**
     * Subscribe to web push notifications
     */
    @PostMapping(ApiConstants.WEB_PUSH_SUBSCRIBE)
    @Operation(summary = "Subscribe to Notifications", description = "Subscribe to web push notifications")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> subscribe(
            @Valid @RequestBody SubscribeRequest request,
            @RequestAttribute("userType") String userType,
            @RequestAttribute("userId") UUID userId) {

        log.info("Subscribe to web push request for user: {} - {}", userType, userId);

        WebPushSubscription subscription = webPushService.subscribe(
                userType,
                userId,
                request.getEndpoint(),
                request.getP256dhKey(),
                request.getAuthKey()
        );

        log.info("Web push subscription created successfully");

        SubscriptionResponse response = SubscriptionResponse.builder()
                .subscribed(true)
                .endpoint(subscription.getEndpoint())
                .message("Successfully subscribed to notifications")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subscribed successfully", response));
    }

    /**
     * Unsubscribe from web push notifications
     */
    @PostMapping(ApiConstants.WEB_PUSH_UNSUBSCRIBE)
    @Operation(summary = "Unsubscribe from Notifications", description = "Unsubscribe from web push notifications")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> unsubscribe(
            @Valid @RequestBody UnsubscribeRequest request) {

        log.info("Unsubscribe from web push request for endpoint: {}", request.getEndpoint());

        webPushService.unsubscribe(request.getEndpoint());

        log.info("Web push subscription removed successfully");

        SubscriptionResponse response = SubscriptionResponse.builder()
                .subscribed(false)
                .endpoint(request.getEndpoint())
                .message("Successfully unsubscribed from notifications")
                .build();

        return ResponseEntity.ok(
                ApiResponse.success("Unsubscribed successfully", response)
        );
    }

    /**
     * Test notification (for testing purposes)
     */
    @PostMapping("/test")
    @Operation(summary = "Test Notification", description = "Send a test notification to current user")
    public ResponseEntity<ApiResponse<String>> testNotification(
            @RequestAttribute("userType") String userType,
            @RequestAttribute("userId") UUID userId) {

        log.info("Test notification request for user: {} - {}", userType, userId);

        webPushService.sendNotification(
                userType,
                userId,
                "Test Notification",
                "This is a test notification from Hospital Meal Ordering System"
        );

        return ResponseEntity.ok(
                ApiResponse.success("Test notification sent successfully")
        );
    }

    /**
     * Get subscription status
     */
    @GetMapping("/status")
    @Operation(summary = "Get Subscription Status", description = "Check if user has active subscriptions")
    public ResponseEntity<ApiResponse<SubscriptionStatusResponse>> getSubscriptionStatus(
            @RequestAttribute("userType") String userType,
            @RequestAttribute("userId") UUID userId) {

        log.debug("Get subscription status for user: {} - {}", userType, userId);

        int subscriptionCount = webPushService.getSubscriptionCount(userType, userId);

        SubscriptionStatusResponse response = SubscriptionStatusResponse.builder()
                .hasSubscription(subscriptionCount > 0)
                .subscriptionCount(subscriptionCount)
                .build();

        return ResponseEntity.ok(
                ApiResponse.success("Subscription status retrieved successfully", response)
        );
    }

    /**
     * Get public VAPID key (for client-side subscription)
     */
    @GetMapping("/public-key")
    @Operation(summary = "Get VAPID public key", description = "Get the public VAPID key for push notification subscription")
    public ResponseEntity<ApiResponse<PublicKeyResponse>> getPublicKey() {
        log.info("Retrieving VAPID public key");

        String publicKey = webPushService.getPublicKey(); // ← Get from service

        PublicKeyResponse response = new PublicKeyResponse(publicKey);

        return ResponseEntity.ok(
                ApiResponse.success("Public key retrieved successfully", response)
        );
    }

    // ========== REQUEST/RESPONSE DTOs ==========

    /**
     * Subscribe request DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscribeRequest {

        @NotBlank(message = "Endpoint is required")
        private String endpoint;

        @NotBlank(message = "P256dh key is required")
        private String p256dhKey;

        @NotBlank(message = "Auth key is required")
        private String authKey;
    }

    /**
     * Unsubscribe request DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnsubscribeRequest {

        @NotBlank(message = "Endpoint is required")
        private String endpoint;
    }

    /**
     * Subscription response DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionResponse {
        private Boolean subscribed;
        private String endpoint;
        private String message;
    }

    /**
     * Subscription status response DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionStatusResponse {
        private Boolean hasSubscription;
        private Integer subscriptionCount;
    }

    /**
     * Public key response DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PublicKeyResponse {
        private String publicKey;
    }
}