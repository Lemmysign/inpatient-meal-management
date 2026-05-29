package com.hospital.meal.repository;

import com.hospital.meal.model.status.MealStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MealStatusRepository extends JpaRepository<MealStatus, UUID> {

    Optional<MealStatus> findByCode(String code);

    @Query("SELECT ms FROM MealStatus ms ORDER BY ms.sortOrder ASC")
    List<MealStatus> findAllOrderedBySort();

    boolean existsByCode(String code);
}