package com.hospital.meal.config;

import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.GeneralSecurityException;
import java.security.Security;

@Configuration
@ConditionalOnProperty(name = "webpush.enabled", havingValue = "true")
@Slf4j
public class WebPushConfig {

    @Value("${webpush.public.key}")
    private String publicKey;

    @Value("${webpush.private.key}")
    private String privateKey;

    @Value("${webpush.subject}")
    private String subject;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Bean
    public PushService pushService() throws GeneralSecurityException {
        log.info("=== WEB PUSH CONFIG ===");
        log.info("Public Key: {}", publicKey != null ? "SET" : "NOT SET");
        log.info("Private Key: {}", privateKey != null ? "SET" : "NOT SET");
        log.info("Subject: {}", subject);
        log.info("=======================");

        if (publicKey == null || publicKey.isEmpty() ||
                privateKey == null || privateKey.isEmpty() ||
                subject == null || subject.isEmpty()) {
            throw new IllegalStateException(
                    "Web Push VAPID keys not configured. " +
                            "publicKey=" + (publicKey != null ? "SET" : "NULL") + ", " +
                            "privateKey=" + (privateKey != null ? "SET" : "NULL") + ", " +
                            "subject=" + subject
            );
        }

        log.info("Web Push service initialized successfully");
        return new PushService(publicKey, privateKey, subject);
    }
}