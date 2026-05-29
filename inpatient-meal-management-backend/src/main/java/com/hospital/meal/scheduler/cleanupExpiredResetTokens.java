package com.hospital.meal.scheduler;

import com.hospital.meal.repository.PasswordResetTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
@Slf4j
public class cleanupExpiredResetTokens {

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Transactional
    @Scheduled(cron = "0 0 */6 * * *") // Every 6 hours
    public void cleanupexpiredresettokens() {
        log.info("Cleaning up expired password reset tokens");

        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(1);
        passwordResetTokenRepository.deleteExpiredTokens(cutoffTime);

        log.info("Expired password reset tokens cleaned up");
    }


}
