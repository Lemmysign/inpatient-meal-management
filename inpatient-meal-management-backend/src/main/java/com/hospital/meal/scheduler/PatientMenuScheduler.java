package com.hospital.meal.scheduler;

import com.hospital.meal.repository.PatientMenuRepository;
import com.hospital.meal.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class PatientMenuScheduler {

    private final PatientMenuRepository patientMenuRepository;

    /**
     * Runs every day at midnight (00:00)
     * Deactivates all patient menus where valid_until < today
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Africa/Lagos")  // Every day at 00:00
    @Transactional
    public void deactivateExpiredMenus() {
        LocalDate today = DateUtil.getCurrentDate();

        log.info("Running scheduled job: Deactivating expired patient menus for date: {}", today);

        int deactivatedCount = patientMenuRepository.deactivateExpiredMenus(today);

        log.info("Scheduled job completed: {} patient menu(s) deactivated", deactivatedCount);
    }
}