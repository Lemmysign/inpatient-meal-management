package com.hospital.meal.security.session;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class DeviceFingerprintUtil {

    /**
     * Generate device fingerprint from HTTP request
     * Combines User-Agent and other browser characteristics
     */
    public String generate(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String acceptLanguage = request.getHeader("Accept-Language");
        String acceptEncoding = request.getHeader("Accept-Encoding");

        String fingerprint = String.format("%s|%s|%s",
                userAgent != null ? userAgent : "",
                acceptLanguage != null ? acceptLanguage : "",
                acceptEncoding != null ? acceptEncoding : ""
        );

        return hashFingerprint(fingerprint);
    }

    /**
     * Hash fingerprint using SHA-256
     */
    private String hashFingerprint(String fingerprint) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fingerprint.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple Base64 encoding
            return Base64.getEncoder().encodeToString(fingerprint.getBytes(StandardCharsets.UTF_8));
        }
    }
}