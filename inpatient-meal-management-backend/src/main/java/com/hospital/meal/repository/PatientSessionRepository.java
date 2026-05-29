package com.hospital.meal.repository;

import com.hospital.meal.model.user.PatientSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientSessionRepository extends JpaRepository<PatientSession, UUID> {

    // Keep JOIN FETCH since we need patient data in session validation
    @Query("SELECT ps FROM PatientSession ps JOIN FETCH ps.patient WHERE ps.sessionToken = :token")
    Optional<PatientSession> findBySessionToken(@Param("token") String token);

    // Keep JOIN FETCH for session validation (needed for patient details)
    @Query("SELECT ps FROM PatientSession ps JOIN FETCH ps.patient WHERE " +
            "ps.sessionToken = :token AND " +
            "ps.isActive = true AND " +
            "ps.expiresAt > :now")
    Optional<PatientSession> findValidSession(@Param("token") String token,
                                              @Param("now") LocalDateTime now);

    // Keep these as-is (still need patient relationship)
    @Query("SELECT ps FROM PatientSession ps JOIN FETCH ps.patient WHERE " +
            "ps.patient.id = :patientId AND " +
            "ps.isActive = true AND " +
            "ps.expiresAt > :now " +
            "ORDER BY ps.createdAt DESC")
    List<PatientSession> findActiveSessionsByPatientId(@Param("patientId") UUID patientId,
                                                       @Param("now") LocalDateTime now);

    // ✅ OPTIMIZED: Use uhid column directly (no join needed)
    @Query("SELECT ps FROM PatientSession ps WHERE " +
            "ps.uhid = :uhid AND " +
            "ps.isActive = true AND " +
            "ps.expiresAt > :now " +
            "ORDER BY ps.createdAt DESC")
    List<PatientSession> findActiveSessionsByUhid(@Param("uhid") String uhid,
                                                  @Param("now") LocalDateTime now);

    // Cleanup queries - no patient data needed
    @Query("SELECT ps FROM PatientSession ps WHERE ps.expiresAt < :cutoff")
    List<PatientSession> findExpiredSessions(@Param("cutoff") LocalDateTime cutoff);

    @Modifying
    @Query("UPDATE PatientSession ps SET ps.isActive = false WHERE ps.expiresAt < :cutoff")
    int deactivateExpiredSessions(@Param("cutoff") LocalDateTime cutoff);

    @Modifying
    @Query("UPDATE PatientSession ps SET ps.isActive = false WHERE ps.patient.id = :patientId")
    int deactivateAllSessionsByPatientId(@Param("patientId") UUID patientId);

    // ✅ ADD THIS: Optimized version using uhid
    @Modifying
    @Query("UPDATE PatientSession ps SET ps.isActive = false WHERE ps.uhid = :uhid")
    int deactivateAllSessionsByUhid(@Param("uhid") String uhid);

    @Modifying
    @Query("DELETE FROM PatientSession ps WHERE ps.expiresAt < :cutoff AND ps.isActive = false")
    int deleteOldInactiveSessions(@Param("cutoff") LocalDateTime cutoff);
}