package com.hospital.meal.event.listener;

import com.hospital.meal.repository.PatientSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionCleanupListener {

    private final PatientSessionRepository patientSessionRepository;

    /**
     * Cleanup expired patient sessions
     * Runs every hour
     */
    @Scheduled(cron = "${session.cleanup.cron:0 0 * * * *}")
    @Transactional
    public void cleanupExpiredSessions() {
        try {
            log.info("Starting expired session cleanup...");

            LocalDateTime now = LocalDateTime.now();

            // Deactivate expired sessions
            int deactivatedCount = patientSessionRepository.deactivateExpiredSessions(now);

            log.info("Deactivated {} expired sessions", deactivatedCount);

            // Delete old inactive sessions (older than configured days)
            LocalDateTime deleteCutoff = now.minusDays(7); // configurable
            int deletedCount = patientSessionRepository.deleteOldInactiveSessions(deleteCutoff);

            log.info("Deleted {} old inactive sessions", deletedCount);
            log.info("Session cleanup completed successfully");

        } catch (Exception e) {
            log.error("Error during session cleanup", e);
        }
    }

    /**
     * Log session statistics
     * Runs every 6 hours
     */
    @Scheduled(fixedRate = 21600000) // 6 hours
    public void logSessionStatistics() {
        try {
            long totalSessions = patientSessionRepository.count();
            long activeSessions = patientSessionRepository.findActiveSessionsByUhid("", LocalDateTime.now()).size();

            log.info("Session Statistics - Total: {}, Active: {}", totalSessions, activeSessions);

        } catch (Exception e) {
            log.error("Error logging session statistics", e);
        }
    }
}