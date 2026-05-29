package com.hospital.meal.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActuatorConfig {

    /**
     * Custom health indicator for the application
     */
    @Bean
    public HealthIndicator customHealthIndicator() {
        return () -> Health.up()
                .withDetail("app", "Hospital Meal Ordering System")
                .withDetail("version", "1.0.0")
                .withDetail("status", "Running")
                .build();
    }
}