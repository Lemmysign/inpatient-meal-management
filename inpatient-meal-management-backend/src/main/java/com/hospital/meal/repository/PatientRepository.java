package com.hospital.meal.repository;

import com.hospital.meal.model.user.Patient;
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
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByUhid(String uhid);

    @Query("SELECT p FROM Patient p WHERE p.uhid = :uhid AND p.isActive = true")
    Optional<Patient> findActiveByUhid(@Param("uhid") String uhid);

    @Query("SELECT p FROM Patient p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "p.uhid LIKE CONCAT('%', :search, '%')")
    Page<Patient> searchPatients(@Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM Patient p WHERE p.isActive = true ORDER BY p.createdAt DESC")
    Page<Patient> findAllActive(Pageable pageable);

    @Query("SELECT p FROM Patient p WHERE " +
            "p.roomNumber = :roomNumber AND " +
            "p.isActive = true")
    List<Patient> findByRoomNumber(@Param("roomNumber") String roomNumber);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.isActive = true")
    long countActivePatients();

    boolean existsByUhid(String uhid);
}