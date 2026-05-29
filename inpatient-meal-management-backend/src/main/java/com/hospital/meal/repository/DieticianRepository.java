package com.hospital.meal.repository;

import com.hospital.meal.model.user.Dietician;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DieticianRepository extends JpaRepository<Dietician, UUID> {

    Optional<Dietician> findByEmail(String email);

    Optional<Dietician> findByStaffId(String staffId);

    @Query("SELECT d FROM Dietician d WHERE d.email = :email AND d.isActive = true")
    Optional<Dietician> findActiveByEmail(@Param("email") String email);

    @Query("SELECT d FROM Dietician d WHERE d.isActive = true ORDER BY d.name ASC")
    List<Dietician> findAllActive();

    @Query("SELECT d FROM Dietician d WHERE d.isActive = true ORDER BY d.name ASC")
    Page<Dietician> findAllActive(Pageable pageable);

    @Query("SELECT d FROM Dietician d WHERE " +
            "LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(d.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Dietician> searchDieticians(@Param("search") String search, Pageable pageable);

    boolean existsByEmail(String email);

    /**
     * Find all dieticians for filter options
     */
    @Query("SELECT d FROM Dietician d WHERE d.isActive = true ORDER BY d.name ASC")
    List<Dietician> findAllActiveForFilter();


    /**
     * Find all menu groups with patient count for filter options
     */


    boolean existsByStaffId(String staffId);
}