package com.hospital.meal.repository;

import com.hospital.meal.model.user.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminRepository extends JpaRepository<Admin, UUID> {

    Optional<Admin> findByEmail(String email);

    Optional<Admin> findByStaffId(String staffId);

    @Query("SELECT a FROM Admin a WHERE a.email = :email AND a.isActive = true")
    Optional<Admin> findActiveByEmail(@Param("email") String email);

    boolean existsByEmail(String email);

    boolean existsByStaffId(String staffId);
}