package com.hospital.meal.repository;

import com.hospital.meal.model.user.KitchenStaff;
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
public interface KitchenStaffRepository extends JpaRepository<KitchenStaff, UUID> {

    Optional<KitchenStaff> findByEmail(String email);

    @Query("SELECT ks FROM KitchenStaff ks WHERE ks.email = :email AND ks.isActive = true")
    Optional<KitchenStaff> findActiveByEmail(@Param("email") String email);

    @Query("SELECT ks FROM KitchenStaff ks WHERE ks.isActive = true ORDER BY ks.name ASC")
    List<KitchenStaff> findAllActive();

    @Query("SELECT ks FROM KitchenStaff ks WHERE ks.isActive = true ORDER BY ks.name ASC")
    Page<KitchenStaff> findAllActive(Pageable pageable);

    @Query("SELECT ks FROM KitchenStaff ks WHERE " +
            "LOWER(ks.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(ks.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<KitchenStaff> searchKitchenStaff(@Param("search") String search, Pageable pageable);

    boolean existsByEmail(String email);
}