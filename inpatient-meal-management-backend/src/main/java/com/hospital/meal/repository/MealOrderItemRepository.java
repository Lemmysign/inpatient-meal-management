package com.hospital.meal.repository;

import com.hospital.meal.model.order.MealOrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MealOrderItemRepository extends JpaRepository<MealOrderItem, UUID> {

    @Query("SELECT moi FROM MealOrderItem moi " +
            "JOIN FETCH moi.mealStatus ms " +
            "JOIN FETCH moi.foodItem fi " +
            "JOIN FETCH moi.mealOrder mo " +
            "JOIN FETCH mo.patient p " +
            "WHERE ms.code = :statusCode AND moi.mealType = :mealType " +
            "ORDER BY moi.orderedAt ASC")
    List<MealOrderItem> findQueueByMealType(@Param("statusCode") String statusCode,
                                            @Param("mealType") String mealType);

    @Query("SELECT moi FROM MealOrderItem moi " +
            "JOIN FETCH moi.mealStatus ms " +
            "JOIN FETCH moi.foodItem fi " +
            "JOIN FETCH moi.mealOrder mo " +
            "JOIN FETCH mo.patient p " +
            "LEFT JOIN FETCH moi.processedByStaff " +
            "WHERE ms.code = :statusCode AND moi.mealType = :mealType " +
            "ORDER BY moi.orderedAt ASC")
    Page<MealOrderItem> findQueueByMealTypePaged(
            @Param("statusCode") String statusCode,
            @Param("mealType") String mealType,
            Pageable pageable);

    @Query("SELECT moi FROM MealOrderItem moi " +
            "JOIN FETCH moi.mealStatus ms " +
            "JOIN FETCH moi.foodItem fi " +
            "WHERE moi.mealOrder.id = :orderId " +
            "ORDER BY moi.mealType, moi.orderedAt")
    List<MealOrderItem> findByMealOrderId(@Param("orderId") UUID orderId);

    @Query("SELECT moi FROM MealOrderItem moi WHERE " +
            "moi.mealOrder.id = :orderId AND " +
            "moi.mealType = :mealType")
    List<MealOrderItem> findByMealOrderIdAndMealType(@Param("orderId") UUID orderId,
                                                     @Param("mealType") String mealType);

    @Query("SELECT ms.code, COUNT(moi) FROM MealOrderItem moi " +
            "JOIN moi.mealStatus ms " +
            "JOIN moi.mealOrder mo " +
            "WHERE mo.orderDate = :date " +
            "GROUP BY ms.code")
    List<Object[]> countByStatusForDate(@Param("date") LocalDate date);

    @Query("SELECT moi.mealType, COUNT(moi) FROM MealOrderItem moi " +
            "JOIN moi.mealOrder mo " +
            "WHERE mo.orderDate = :date " +
            "GROUP BY moi.mealType")
    List<Object[]> countByMealTypeForDate(@Param("date") LocalDate date);

    @Query("SELECT ms.code, moi.mealType, COUNT(moi) FROM MealOrderItem moi " +
            "JOIN moi.mealStatus ms " +
            "JOIN moi.mealOrder mo " +
            "WHERE mo.orderDate = :date " +
            "GROUP BY ms.code, moi.mealType")
    List<Object[]> countByStatusAndMealTypeForDate(@Param("date") LocalDate date);

    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (moi.processed_at - moi.ordered_at)) / 60) " +
            "FROM meal_order_items moi " +
            "JOIN meal_statuses ms ON moi.meal_status_id = ms.id " +
            "JOIN meal_orders mo ON moi.meal_order_id = mo.id " +
            "WHERE ms.code = 'PROCESSED' AND mo.order_date = :date",
            nativeQuery = true)
    Double calculateAverageProcessingTimeForDate(@Param("date") LocalDate date);

    @Query("SELECT EXTRACT(HOUR FROM moi.orderedAt), COUNT(moi) FROM MealOrderItem moi " +
            "JOIN moi.mealOrder mo " +
            "WHERE mo.orderDate = :date " +
            "GROUP BY EXTRACT(HOUR FROM moi.orderedAt) " +
            "ORDER BY COUNT(moi) DESC")
    List<Object[]> findPeakOrderingHoursForDate(@Param("date") LocalDate date);

    @Query("SELECT fi.name, COUNT(moi) FROM MealOrderItem moi " +
            "JOIN moi.foodItem fi " +
            "JOIN moi.mealOrder mo " +
            "WHERE mo.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY fi.id, fi.name " +
            "ORDER BY COUNT(moi) DESC")
    List<Object[]> findMostOrderedFoodItems(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate,
                                            Pageable pageable);

    @Query("SELECT moi FROM MealOrderItem moi " +
            "JOIN FETCH moi.mealStatus ms " +
            "WHERE moi.mealOrder.id = :orderId AND " +
            "moi.mealType = :mealType AND " +
            "ms.code = 'PENDING'")
    List<MealOrderItem> findModifiableItem(@Param("orderId") UUID orderId,
                                           @Param("mealType") String mealType);

    @Query("SELECT moi FROM MealOrderItem moi " +
            "JOIN FETCH moi.mealStatus ms " +
            "JOIN FETCH moi.foodItem fi " +
            "JOIN FETCH moi.mealOrder mo " +
            "JOIN FETCH mo.patient p " +
            "LEFT JOIN FETCH moi.processedByStaff " +
            "WHERE ms.code = :statusCode " +
            "ORDER BY moi.orderedAt ASC")
    Page<MealOrderItem> findQueueByStatusPaged(
            @Param("statusCode") String statusCode,
            Pageable pageable);

    @Query("SELECT moi FROM MealOrderItem moi " +
            "JOIN FETCH moi.mealStatus ms " +
            "JOIN FETCH moi.foodItem fi " +
            "JOIN FETCH moi.mealOrder mo " +
            "JOIN FETCH mo.patient p " +
            "LEFT JOIN FETCH moi.processedByStaff " +
            "WHERE ms.code = :statusCode " +
            "AND mo.orderDate BETWEEN :startDate AND :endDate " +
            "ORDER BY moi.orderedAt ASC")
    Page<MealOrderItem> findQueueByStatusAndDateRangePaged(
            @Param("statusCode") String statusCode,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query("SELECT moi FROM MealOrderItem moi " +
            "JOIN FETCH moi.mealStatus ms " +
            "JOIN FETCH moi.foodItem fi " +
            "JOIN FETCH moi.mealOrder mo " +
            "JOIN FETCH mo.patient p " +
            "LEFT JOIN FETCH moi.processedByStaff " +
            "WHERE ms.code = :statusCode " +
            "AND moi.mealType = :mealType " +
            "AND mo.orderDate = :date " +
            "ORDER BY moi.orderedAt ASC")
    Page<MealOrderItem> findQueueByStatusAndMealTypeAndDatePaged(
            @Param("statusCode") String statusCode,
            @Param("mealType") String mealType,
            @Param("date") LocalDate date,
            Pageable pageable);

    @Query("SELECT moi.mealType, COUNT(moi) FROM MealOrderItem moi " +
            "JOIN moi.mealStatus ms " +
            "JOIN moi.mealOrder mo " +
            "WHERE ms.code = :statusCode " +
            "AND mo.orderDate = :date " +
            "GROUP BY moi.mealType")
    List<Object[]> countByStatusAndMealTypeForToday(
            @Param("statusCode") String statusCode,
            @Param("date") LocalDate date);

    @Query("SELECT moi FROM MealOrderItem moi " +
            "JOIN FETCH moi.mealStatus ms " +
            "JOIN FETCH moi.foodItem fi " +
            "JOIN FETCH moi.mealOrder mo " +
            "JOIN FETCH mo.patient p " +
            "LEFT JOIN FETCH moi.processedByStaff " +
            "WHERE ms.code = :statusCode " +
            "AND moi.mealType = :mealType " +
            "AND mo.orderDate = :date " +
            "AND (:searchTerm IS NULL OR " +
            "     LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:searchTerm AS string), '%')) OR " +
            "     LOWER(p.uhid) LIKE LOWER(CONCAT('%', CAST(:searchTerm AS string), '%'))) " +
            "ORDER BY moi.orderedAt ASC")
    Page<MealOrderItem> searchQueueByStatusAndMealTypeAndDate(
            @Param("statusCode") String statusCode,
            @Param("mealType") String mealType,
            @Param("date") LocalDate date,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    @Query("SELECT moi FROM MealOrderItem moi " +
            "JOIN FETCH moi.mealOrder mo " +
            "JOIN FETCH mo.patient p " +
            "JOIN FETCH moi.foodItem fi " +
            "WHERE moi.mealType = :mealType " +
            "AND FUNCTION('DATE', moi.orderedAt) = :orderedDate " +
            "ORDER BY moi.orderedAt ASC")
    Page<MealOrderItem> findByMealTypeAndOrderedDatePaged(
            @Param("mealType") String mealType,
            @Param("orderedDate") LocalDate orderedDate,
            Pageable pageable);

    @Query("SELECT moi FROM MealOrderItem moi " +
            "JOIN FETCH moi.mealOrder mo " +
            "JOIN FETCH mo.patient p " +
            "JOIN FETCH moi.foodItem fi " +
            "WHERE moi.mealType = :mealType " +
            "AND FUNCTION('DATE', moi.orderedAt) = :orderedDate " +
            "ORDER BY moi.orderedAt ASC")
    List<MealOrderItem> findByMealTypeAndOrderedDate(
            @Param("mealType") String mealType,
            @Param("orderedDate") LocalDate orderedDate);

    @Query("SELECT ms.code, COUNT(moi) FROM MealOrderItem moi " +
            "JOIN moi.mealStatus ms " +
            "JOIN moi.mealOrder mo " +
            "WHERE mo.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY ms.code")
    List<Object[]> countByStatusForDateRange(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    @Query("SELECT moi.mealType, COUNT(moi) FROM MealOrderItem moi " +
            "JOIN moi.mealOrder mo " +
            "WHERE mo.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY moi.mealType")
    List<Object[]> countByMealTypeForDateRange(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (moi.processed_at - moi.ordered_at)) / 60) " +
            "FROM meal_order_items moi " +
            "JOIN meal_statuses ms ON moi.meal_status_id = ms.id " +
            "JOIN meal_orders mo ON moi.meal_order_id = mo.id " +
            "WHERE ms.code = 'PROCESSED' AND mo.order_date BETWEEN :startDate AND :endDate",
            nativeQuery = true)
    Double calculateAverageProcessingTimeForDateRange(@Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    @Query("SELECT EXTRACT(HOUR FROM moi.orderedAt), COUNT(moi) FROM MealOrderItem moi " +
            "JOIN moi.mealOrder mo " +
            "WHERE mo.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY EXTRACT(HOUR FROM moi.orderedAt) " +
            "ORDER BY COUNT(moi) DESC")
    List<Object[]> findPeakOrderingHoursForDateRange(@Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);
}