package com.hospital.meal.security.ratelimit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ratelimit")
@Getter
@Setter
public class RateLimitProperties {

    private boolean enabled = true;

    private RateConfig patientLogin = new RateConfig(10);
    private RateConfig patientOrder = new RateConfig(5);
    private RateConfig staffLogin = new RateConfig(10);
    private RateConfig global = new RateConfig(200);
    private RateConfig forgotPassword = new RateConfig(3);
    private RateConfig resetPassword = new RateConfig(5);

    @Getter
    @Setter
    public static class RateConfig {
        private int requestsPerMinute;

        public RateConfig(int defaultValue) {
            this.requestsPerMinute = defaultValue;
        }
    }
}