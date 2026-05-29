package com.hospital.meal.security.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@Slf4j
@RequiredArgsConstructor
public class RateLimitService {

    private static final int CLEANUP_INTERVAL_MS = 30 * 60 * 1000; // 30 minutes
    private static final int CLEANUP_RETENTION_MINUTES = 30;

    private final RateLimitProperties properties;

    // Key: identifier (IP or UHID), Value: Queue of request timestamps
    private final Map<String, Queue<LocalDateTime>> requestTimestamps = new ConcurrentHashMap<>();

    /**
     * Core sliding window rate limit check
     */
    public boolean isAllowed(String identifier, int maxRequests, Duration window) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minus(window);

        Queue<LocalDateTime> timestamps = requestTimestamps.computeIfAbsent(
                identifier,
                k -> new ConcurrentLinkedQueue<>()
        );

        // Remove requests outside the window
        timestamps.removeIf(timestamp -> timestamp.isBefore(windowStart));

        // Check if limit exceeded
        if (timestamps.size() >= maxRequests) {
            log.warn("Rate limit exceeded for identifier: {}", identifier);
            return false;
        }

        timestamps.add(now);
        return true;
    }

    /**
     * Patient login rate limit — IP-based (10/min)
     */
    public boolean isLoginAllowed(String ipAddress) {
        if (!properties.isEnabled()) return true;
        return isAllowed(
                "login:" + ipAddress,
                properties.getPatientLogin().getRequestsPerMinute(),
                Duration.ofMinutes(1)
        );
    }

    /**
     * Patient order rate limit — UHID-based (5/min)
     */
    public boolean isOrderAllowed(String uhid) {
        if (!properties.isEnabled()) return true;
        return isAllowed(
                "order:" + uhid,
                properties.getPatientOrder().getRequestsPerMinute(),
                Duration.ofMinutes(1)
        );
    }

    /**
     * Staff login rate limit — IP-based (10/min)
     */
    public boolean isStaffLoginAllowed(String ipAddress) {
        if (!properties.isEnabled()) return true;
        return isAllowed(
                "staff-login:" + ipAddress,
                properties.getStaffLogin().getRequestsPerMinute(),
                Duration.ofMinutes(1)
        );
    }

    /**
     * Global rate limit — IP-based (200/min)
     * Baseline protection for all endpoints
     */
    public boolean isGlobalAllowed(String ipAddress) {
        if (!properties.isEnabled()) return true;
        return isAllowed(
                "global:" + ipAddress,
                properties.getGlobal().getRequestsPerMinute(),
                Duration.ofMinutes(1)
        );
    }

    /**
     * Forgot password rate limit — IP-based (3/min)
     * Prevents email spam abuse
     */
    public boolean isForgotPasswordAllowed(String ipAddress) {
        if (!properties.isEnabled()) return true;
        return isAllowed(
                "forgot-password:" + ipAddress,
                properties.getForgotPassword().getRequestsPerMinute(),
                Duration.ofMinutes(1)
        );
    }

    /**
     * Reset password rate limit — IP-based (5/min)
     */
    public boolean isResetPasswordAllowed(String ipAddress) {
        if (!properties.isEnabled()) return true;
        return isAllowed(
                "reset-password:" + ipAddress,
                properties.getResetPassword().getRequestsPerMinute(),
                Duration.ofMinutes(1)
        );
    }

    /**
     * Scheduled cleanup of old entries every 30 minutes
     */
    @Scheduled(fixedRate = CLEANUP_INTERVAL_MS)
    public void cleanup() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(CLEANUP_RETENTION_MINUTES);

        int removedEntries = 0;
        int totalTimestamps = 0;

        for (Map.Entry<String, Queue<LocalDateTime>> entry : requestTimestamps.entrySet()) {
            Queue<LocalDateTime> timestamps = entry.getValue();
            int beforeSize = timestamps.size();
            timestamps.removeIf(timestamp -> timestamp.isBefore(cutoff));
            removedEntries += (beforeSize - timestamps.size());
            totalTimestamps += timestamps.size();
        }

        int beforeMapSize = requestTimestamps.size();
        requestTimestamps.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        int removedQueues = beforeMapSize - requestTimestamps.size();

        log.info("Rate limit cleanup completed. Active identifiers: {}, " +
                        "Active timestamps: {}, Removed timestamps: {}, Removed queues: {}",
                requestTimestamps.size(), totalTimestamps, removedEntries, removedQueues);
    }
}