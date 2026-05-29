package com.hospital.meal.repository;

import com.hospital.meal.model.menu.PatientMenu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientMenuRepository extends JpaRepository<PatientMenu, UUID>,
        JpaSpecificationExecutor<PatientMenu> {

    @Query("SELECT pm FROM PatientMenu pm WHERE " +
            "pm.patient.id = :patientId AND " +
            "pm.isActive = true AND " +
            "pm.validFrom <= :date AND " +
            "(pm.validUntil IS NULL OR pm.validUntil >= :date)")
    List<PatientMenu> findActiveMenusByPatientIdAndDate(@Param("patientId") UUID patientId,
                                                        @Param("date") LocalDate date);

    @Query("SELECT pm FROM PatientMenu pm WHERE " +
            "pm.patient.uhid = :uhid AND " +
            "pm.isActive = true AND " +
            "pm.validFrom <= :date AND " +
            "(pm.validUntil IS NULL OR pm.validUntil >= :date)")
    List<PatientMenu> findActiveMenusByUhidAndDate(@Param("uhid") String uhid,
                                                   @Param("date") LocalDate date);

    @Query("SELECT pm FROM PatientMenu pm WHERE " +
            "pm.patient.id = :patientId AND " +
            "pm.isActive = true " +
            "ORDER BY pm.validFrom DESC")
    List<PatientMenu> findByPatientId(@Param("patientId") UUID patientId);

    @Query("SELECT pm FROM PatientMenu pm WHERE " +
            "pm.assignedByDietician.id = :dieticianId AND " +
            "pm.isActive = true " +
            "ORDER BY pm.assignedAt DESC")
    Page<PatientMenu> findByDieticianId(@Param("dieticianId") UUID dieticianId, Pageable pageable);

    @Query("SELECT pm FROM PatientMenu pm WHERE " +
            "pm.menuGroup.id = :menuGroupId AND " +
            "pm.isActive = true")
    List<PatientMenu> findByMenuGroupId(@Param("menuGroupId") UUID menuGroupId);

    @Query("SELECT COUNT(pm) FROM PatientMenu pm WHERE " +
            "pm.patient.id = :patientId AND " +
            "pm.menuGroup.id = :menuGroupId AND " +
            "pm.validFrom = :validFrom AND " +
            "pm.isActive = true")
    long countDuplicateAssignment(@Param("patientId") UUID patientId,
                                  @Param("menuGroupId") UUID menuGroupId,
                                  @Param("validFrom") LocalDate validFrom);

    /**
     * Search patient menus with dynamic filters
     */

    /**
     * Deactivate all expired patient menus
     * Called by scheduler daily at midnights
     */
    @Modifying
    @Query("UPDATE PatientMenu pm SET pm.isActive = false " +
            "WHERE pm.isActive = true " +
            "AND pm.validUntil IS NOT NULL " +
            "AND pm.validUntil < :today")
    int deactivateExpiredMenus(@Param("today") LocalDate today);

    @Query("""
            SELECT pm FROM PatientMenu pm
            JOIN FETCH pm.patient
            WHERE pm.assignedByDietician.id = :dieticianId
            AND pm.isActive = true
            """)
    List<PatientMenu> findActiveMenusByDieticianId(@Param("dieticianId") UUID dieticianId);


    @Query("""
        SELECT pm FROM PatientMenu pm
        JOIN FETCH pm.patient
        JOIN FETCH pm.menuGroup
        JOIN FETCH pm.assignedByDietician
        WHERE pm.id = :id
        """)
    Optional<PatientMenu> findByIdWithDetails(@Param("id") UUID id);


    @Query("SELECT COUNT(pm) FROM PatientMenu pm " +
            "WHERE pm.patient.id = :patientId " +
            "AND pm.menuGroup.id = :menuGroupId " +
            "AND pm.isActive = true " +
            "AND pm.id != :excludeId " +  // ← Exclude current assignment
            "AND pm.validFrom = :validFrom")
    long countDuplicateAssignmentExcludingCurrent(
            @Param("patientId") UUID patientId,
            @Param("menuGroupId") UUID menuGroupId,
            @Param("validFrom") LocalDate validFrom,
            @Param("excludeId") UUID excludeId  // ← The current assignment ID
    );



    long countByMenuGroupIdAndIsActiveTrue(UUID menuGroupId);


}