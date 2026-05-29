package com.hospital.meal.repository;

import com.hospital.meal.model.notification.WebPushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WebPushSubscriptionRepository extends JpaRepository<WebPushSubscription, UUID> {

    Optional<WebPushSubscription> findByEndpoint(String endpoint);

    @Query("SELECT wps FROM WebPushSubscription wps WHERE " +
            "wps.userType = :userType AND wps.userId = :userId")
    List<WebPushSubscription> findByUserTypeAndUserId(@Param("userType") String userType,
                                                      @Param("userId") UUID userId);

    @Query("SELECT wps FROM WebPushSubscription wps WHERE wps.userType = :userType")
    List<WebPushSubscription> findByUserType(@Param("userType") String userType);

    void deleteByEndpoint(String endpoint);
}