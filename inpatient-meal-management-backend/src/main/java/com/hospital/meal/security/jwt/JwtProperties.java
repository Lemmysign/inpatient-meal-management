package com.hospital.meal.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    private String secret;
    private Long expirationMs;
    private Long patientSessionExpirationMinutes; // Changed from Ms to Minutes

    // Default values
    public JwtProperties() {
        this.expirationMs = 86400000L; // 24 hours
        this.patientSessionExpirationMinutes = 120L; // 2 hours (changed from 900000L)
    }
}