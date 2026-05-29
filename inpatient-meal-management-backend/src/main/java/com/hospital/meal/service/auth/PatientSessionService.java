package com.hospital.meal.service.auth;

import com.hospital.meal.model.user.Patient;
import com.hospital.meal.model.user.PatientSession;

import java.util.Optional;
import java.util.UUID;

public interface PatientSessionService {

    /**
     * Create a new patient session
     */
    PatientSession createSession(Patient patient, String ipAddress, String userAgent);

    /**
     * Get valid session by token
     */
    Optional<PatientSession> getValidSession(String token);

    /**
     * Update session activity timestamp
     */
    void updateActivity(UUID sessionId);

    /**
     * Invalidate specific session
     */
    void invalidateSession(String token);

    /**
     * Invalidate all sessions for a patient
     */
    void invalidateAllPatientSessions(UUID patientId);

    /**
     * Cleanup expired sessions (scheduled task)
     */
    /**void cleanupExpiredSessions();**/
}