package com.hospital.meal.repository;

import com.hospital.meal.model.audit.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    @Query("SELECT al FROM AuditLog al WHERE " +
            "al.userType = :userType AND al.userId = :userId " +
            "ORDER BY al.timestamp DESC")
    Page<AuditLog> findByUserTypeAndUserId(@Param("userType") String userType,
                                           @Param("userId") UUID userId,
                                           Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE " +
            "al.entityType = :entityType AND al.entityId = :entityId " +
            "ORDER BY al.timestamp DESC")
    List<AuditLog> findByEntityTypeAndEntityId(@Param("entityType") String entityType,
                                               @Param("entityId") UUID entityId);

    @Query("SELECT al FROM AuditLog al WHERE " +
            "al.action = :action AND " +
            "al.timestamp BETWEEN :startDate AND :endDate " +
            "ORDER BY al.timestamp DESC")
    Page<AuditLog> findByActionAndTimestampBetween(@Param("action") String action,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate,
                                                   Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE " +
            "al.timestamp BETWEEN :startDate AND :endDate " +
            "ORDER BY al.timestamp DESC")
    Page<AuditLog> findByTimestampBetween(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate,
                                          Pageable pageable);
}