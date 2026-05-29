package com.hospital.meal.repository;

import com.hospital.meal.model.order.OrderModificationLog;
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
public interface OrderModificationLogRepository extends JpaRepository<OrderModificationLog, UUID> {

    @Query("SELECT oml FROM OrderModificationLog oml WHERE " +
            "oml.patient.id = :patientId " +
            "ORDER BY oml.modifiedAt DESC")
    List<OrderModificationLog> findByPatientId(@Param("patientId") UUID patientId);

    @Query("SELECT oml FROM OrderModificationLog oml WHERE " +
            "oml.mealOrder.id = :orderId " +
            "ORDER BY oml.modifiedAt DESC")
    List<OrderModificationLog> findByMealOrderId(@Param("orderId") UUID orderId);

    @Query("SELECT oml FROM OrderModificationLog oml WHERE " +
            "oml.mealOrderItem.id = :itemId " +
            "ORDER BY oml.modifiedAt DESC")
    List<OrderModificationLog> findByMealOrderItemId(@Param("itemId") UUID itemId);

    @Query("SELECT oml FROM OrderModificationLog oml WHERE " +
            "oml.modifiedAt BETWEEN :startDate AND :endDate " +
            "ORDER BY oml.modifiedAt DESC")
    Page<OrderModificationLog> findByModifiedAtBetween(@Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate,
                                                       Pageable pageable);

    // ✅ FIXED: Use native query for PostgreSQL date comparison
    @Query(value = "SELECT COUNT(*) FROM order_modification_logs " +
            "WHERE patient_id = :patientId " +
            "AND DATE(modified_at) = CURRENT_DATE",
            nativeQuery = true)
    long countTodayModificationsByPatientId(@Param("patientId") UUID patientId);
}