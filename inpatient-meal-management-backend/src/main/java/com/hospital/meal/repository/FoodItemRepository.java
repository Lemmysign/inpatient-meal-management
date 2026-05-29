package com.hospital.meal.repository;

import com.hospital.meal.model.menu.FoodItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, UUID> {

    @Query("SELECT fi FROM FoodItem fi WHERE " +
            "fi.menuGroup.id = :menuGroupId AND " +
            "fi.isActive = true " +
            "ORDER BY fi.mealType, fi.name")
    List<FoodItem> findByMenuGroupId(@Param("menuGroupId") UUID menuGroupId);

    @Query("SELECT fi FROM FoodItem fi WHERE " +
            "fi.menuGroup.id = :menuGroupId AND " +
            "(fi.mealType = :mealType OR fi.mealType IS NULL) AND " +
            "fi.isActive = true " +
            "ORDER BY fi.name")
    List<FoodItem> findByMenuGroupIdAndMealType(@Param("menuGroupId") UUID menuGroupId,
                                                @Param("mealType") String mealType);

    @Query("SELECT fi FROM FoodItem fi WHERE " +
            "fi.menuGroup.id IN :menuGroupIds AND " +
            "fi.isActive = true " +
            "ORDER BY fi.menuGroup.name, fi.mealType, fi.name")
    List<FoodItem> findByMenuGroupIds(@Param("menuGroupIds") List<UUID> menuGroupIds);

    @Query("SELECT fi FROM FoodItem fi WHERE " +
            "fi.mealType = :mealType AND " +
            "fi.isActive = true " +
            "ORDER BY fi.name")
    List<FoodItem> findByMealType(@Param("mealType") String mealType);

    @Query("SELECT fi FROM FoodItem fi WHERE " +
            "LOWER(fi.name) LIKE LOWER(CONCAT('%', :search, '%')) AND " +
            "fi.isActive = true")
    Page<FoodItem> searchFoodItems(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(fi) FROM FoodItem fi WHERE fi.menuGroup.id = :menuGroupId AND fi.isActive = true")
    long countByMenuGroupId(@Param("menuGroupId") UUID menuGroupId);

    /**
     * Find all active food items (paginated)
     */
    @Query("SELECT fi FROM FoodItem fi " +
            "WHERE fi.isActive = true " +
            "ORDER BY fi.name ASC")
    Page<FoodItem> findAllActive(Pageable pageable);

    /**
     * Find food items by menu group (paginated)
     */
    @Query("SELECT fi FROM FoodItem fi " +
            "WHERE fi.menuGroup.id = :menuGroupId " +
            "AND fi.isActive = true")
    Page<FoodItem> findByMenuGroupIdPaged(
            @Param("menuGroupId") UUID menuGroupId,
            Pageable pageable);

    /**
     * Find food items by meal type (paginated)
     */
    @Query("SELECT fi FROM FoodItem fi " +
            "WHERE fi.mealType = :mealType " +
            "AND fi.isActive = true")
    Page<FoodItem> findByMealTypePaged(
            @Param("mealType") String mealType,
            Pageable pageable);

    /**
     * Find food items by menu group and meal type (paginated)
     */
    @Query("SELECT fi FROM FoodItem fi " +
            "WHERE fi.menuGroup.id = :menuGroupId " +
            "AND fi.mealType = :mealType " +
            "AND fi.isActive = true")
    Page<FoodItem> findByMenuGroupIdAndMealTypePaged(
            @Param("menuGroupId") UUID menuGroupId,
            @Param("mealType") String mealType,
            Pageable pageable);


    /**
     * Count all active food items
     */
    @Query("SELECT COUNT(fi) FROM FoodItem fi WHERE fi.isActive = true")
    Long countActiveFoodItems();


}