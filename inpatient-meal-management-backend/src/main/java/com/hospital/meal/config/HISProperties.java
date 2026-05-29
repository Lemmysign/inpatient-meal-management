package com.hospital.meal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * HIS scraper configuration properties.
 * Values are loaded from application.properties under his.scraper.*
 */
@ConfigurationProperties(prefix = "his.scraper")
@Getter
@Setter
public class HISProperties {

    /** HIS base URL e.g. http://10.20.20.101:10031 */
    private String baseUrl;

    /** HIS login username */
    private String username;

    /** HIS login password */
    private String password;

    /** Keep-alive interval in minutes (default 8, under 10 min HIS timeout) */
    private int keepAliveIntervalMinutes = 8;

    /** Connection timeout in milliseconds */
    private int connectionTimeoutMs = 10000;

    /** Read timeout in milliseconds */
    private int readTimeoutMs = 15000;
}