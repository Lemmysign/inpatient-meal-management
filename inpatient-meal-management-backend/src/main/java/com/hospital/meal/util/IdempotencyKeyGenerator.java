package com.hospital.meal.util;

import java.time.LocalDate;
import java.util.UUID;

public final class IdempotencyKeyGenerator {

    private IdempotencyKeyGenerator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Generate idempotency key for meal order
     * Format: order:<uhid>:<date>
     */
    public static String generateOrderKey(String uhid, LocalDate orderDate) {
        if (uhid == null || orderDate == null) {
            throw new IllegalArgumentException("UHID and order date cannot be null");
        }
        return String.format("order:%s:%s", uhid, orderDate);
    }

    /**
     * Generate idempotency key for patient session
     * Format: session:<uhid>:<timestamp>
     */
    public static String generateSessionKey(String uhid) {
        if (uhid == null) {
            throw new IllegalArgumentException("UHID cannot be null");
        }
        return String.format("session:%s:%d", uhid, System.currentTimeMillis());
    }

    /**
     * Generate unique token for dietician invite
     */
    public static String generateInviteToken() {
        return UUID.randomUUID().toString();
    }
}