package com.hospital.meal.repository;

import com.hospital.meal.model.menu.MenuGroup;
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
public interface MenuGroupRepository extends JpaRepository<MenuGroup, UUID> {

    Optional<MenuGroup> findByName(String name);

    @Query("SELECT mg FROM MenuGroup mg WHERE mg.isActive = true ORDER BY mg.name ASC")
    List<MenuGroup> findAllActive();

    @Query("SELECT mg FROM MenuGroup mg WHERE mg.isActive = true ORDER BY mg.name ASC")
    Page<MenuGroup> findAllActive(Pageable pageable);

    @Query("SELECT mg FROM MenuGroup mg WHERE mg.isPredefined = true AND mg.isActive = true")
    List<MenuGroup> findPredefinedActive();

    @Query("SELECT mg FROM MenuGroup mg WHERE " +
            "mg.createdByDietician.id = :dieticianId AND " +
            "mg.isActive = true " +
            "ORDER BY mg.name ASC")
    List<MenuGroup> findByDieticianId(@Param("dieticianId") UUID dieticianId);

    @Query("SELECT mg FROM MenuGroup mg WHERE " +
            "LOWER(mg.name) LIKE LOWER(CONCAT('%', :search, '%')) AND " +
            "mg.isActive = true")
    Page<MenuGroup> searchMenuGroups(@Param("search") String search, Pageable pageable);

    /**
     * Find à la carte menu group
     */
    @Query("SELECT mg FROM MenuGroup mg WHERE mg.isAlacarte = true AND mg.isActive = true")
    Optional<MenuGroup> findAlacarteMenuGroup();

    /**
     * Find all menu groups with patient count for filter options
     */
    @Query("SELECT mg, COUNT(pm) FROM MenuGroup mg " +
            "LEFT JOIN PatientMenu pm ON pm.menuGroup.id = mg.id AND pm.isActive = true " +
            "WHERE mg.isActive = true " +
            "GROUP BY mg.id " +
            "ORDER BY mg.name ASC")
    List<Object[]> findAllActiveWithPatientCount();
    @Query("SELECT COUNT(mg) FROM MenuGroup mg WHERE mg.createdByDietician.id = :dieticianId AND mg.isActive = true")
    Long countByDieticianId(@Param("dieticianId") UUID dieticianId);


    @Query("SELECT mg, COUNT(pm) FROM MenuGroup mg " +
            "LEFT JOIN PatientMenu pm ON pm.menuGroup.id = mg.id AND pm.isActive = true " +
            "GROUP BY mg.id " +
            "ORDER BY mg.createdAt DESC")
    List<Object[]> findAllWithPatientCount();


    boolean existsByName(String name);
}