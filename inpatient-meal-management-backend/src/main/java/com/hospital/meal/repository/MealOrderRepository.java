package com.hospital.meal.repository;

import com.hospital.meal.model.order.MealOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface MealOrderRepository extends JpaRepository<MealOrder, UUID> {

    Optional<MealOrder> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT mo FROM MealOrder mo WHERE " +
            "mo.patient.id = :patientId AND " +
            "mo.orderDate = :orderDate")
    Optional<MealOrder> findByPatientIdAndOrderDate(@Param("patientId") UUID patientId,
                                                    @Param("orderDate") LocalDate orderDate);

    // ✅ OPTIMIZED: Query uhid column directly (no join needed)
    @Query("SELECT mo FROM MealOrder mo WHERE " +
            "mo.uhid = :uhid AND " +
            "mo.orderDate = :orderDate")
    Optional<MealOrder> findByUhidAndOrderDate(@Param("uhid") String uhid,
                                               @Param("orderDate") LocalDate orderDate);

    @Query("SELECT mo FROM MealOrder mo WHERE " +
            "mo.patient.id = :patientId " +
            "ORDER BY mo.orderDate DESC")
    List<MealOrder> findByPatientId(@Param("patientId") UUID patientId);

    // ✅ OPTIMIZED: Query uhid column directly (no join needed)
    @Query("SELECT mo FROM MealOrder mo WHERE " +
            "mo.uhid = :uhid " +
            "ORDER BY mo.orderDate DESC")
    List<MealOrder> findByUhid(@Param("uhid") String uhid);

    @Query("SELECT mo FROM MealOrder mo WHERE " +
            "mo.orderDate = :orderDate " +
            "ORDER BY mo.createdAt ASC")
    List<MealOrder> findByOrderDate(@Param("orderDate") LocalDate orderDate);

    @Query("SELECT mo FROM MealOrder mo WHERE " +
            "mo.orderDate BETWEEN :startDate AND :endDate " +
            "ORDER BY mo.orderDate DESC, mo.createdAt DESC")
    Page<MealOrder> findByOrderDateBetween(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate,
                                           Pageable pageable);

    @Query("SELECT COUNT(mo) FROM MealOrder mo WHERE mo.orderDate = :orderDate")
    long countByOrderDate(@Param("orderDate") LocalDate orderDate);

    @Query("""
            SELECT DISTINCT mo FROM MealOrder mo
            JOIN FETCH mo.patient p
            LEFT JOIN FETCH mo.items
            WHERE p.id IN :patientIds
            AND mo.orderDate BETWEEN :startDate AND :endDate
            ORDER BY mo.orderDate DESC
            """)
    List<MealOrder> findByPatientIdsAndDateRange(@Param("patientIds") Set<UUID> patientIds,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    @Query("""
            SELECT COUNT(mo) FROM MealOrder mo
            JOIN mo.patient p
            WHERE p.id IN :patientIds
            AND mo.orderDate = :date
            """)
    Long countByPatientIdsAndDate(@Param("patientIds") Set<UUID> patientIds,
                                  @Param("date") LocalDate date);






}