package com.hospital.meal.service.service_impl.auth;

import com.hospital.meal.model.user.Patient;
import com.hospital.meal.model.user.PatientSession;
import com.hospital.meal.repository.PatientSessionRepository;
import com.hospital.meal.security.jwt.JwtProperties;
import com.hospital.meal.security.jwt.JwtTokenProvider;
import com.hospital.meal.service.auth.PatientSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientSessionServiceImpl implements PatientSessionService {

    private final PatientSessionRepository patientSessionRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public PatientSession createSession(Patient patient, String ipAddress, String userAgent) {
        log.info("Creating session for patient: {}", patient.getUhid());

        // Generate session token
        String sessionToken = jwtTokenProvider.generatePatientToken(patient);

        // Calculate expiration
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusMinutes(jwtProperties.getPatientSessionExpirationMinutes());

        // Create session
        PatientSession session = PatientSession.builder()
                .patient(patient)
                .uhid(patient.getUhid())  // ✅ ADD THIS LINE
                .sessionToken(sessionToken)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiresAt(expiresAt)
                .lastActivityAt(LocalDateTime.now())
                .isActive(true)
                .build();

        return patientSessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PatientSession> getValidSession(String token) {
        return patientSessionRepository.findValidSession(token, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void updateActivity(UUID sessionId) {
        patientSessionRepository.findById(sessionId).ifPresent(session -> {
            session.setLastActivityAt(LocalDateTime.now());
            patientSessionRepository.save(session);
        });
    }

    @Override
    @Transactional
    public void invalidateSession(String token) {
        log.info("Invalidating session");

        patientSessionRepository.findBySessionToken(token).ifPresent(session -> {
            session.setIsActive(false);
            patientSessionRepository.save(session);
        });
    }

    @Override
    @Transactional
    public void invalidateAllPatientSessions(UUID patientId) {
        log.info("Invalidating all sessions for patient: {}", patientId);
        patientSessionRepository.deactivateAllSessionsByPatientId(patientId);
    }
}