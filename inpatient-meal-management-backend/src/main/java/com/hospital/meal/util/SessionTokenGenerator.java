package com.hospital.meal.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class SessionTokenGenerator {

    private SessionTokenGenerator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Generate patient session token (JWT)
     */
    public static String generatePatientSessionToken(UUID patientId, String uhid, String secretKey, long expirationMinutes) {
        if (patientId == null || uhid == null || secretKey == null) {
            throw new IllegalArgumentException("Patient ID, UHID, and secret key cannot be null");
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);
        Date expiryDate = Date.from(expiresAt.atZone(ZoneId.systemDefault()).toInstant());

        Map<String, Object> claims = new HashMap<>();
        claims.put("patientId", patientId.toString());
        claims.put("uhid", uhid);
        claims.put("type", "PATIENT_SESSION");

        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(uhid)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generate random session ID
     */
    public static String generateSessionId() {
        return UUID.randomUUID().toString();
    }
}