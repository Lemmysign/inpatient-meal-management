package com.hospital.meal.repository;

import com.hospital.meal.model.user.DieticianInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DieticianInviteRepository extends JpaRepository<DieticianInvite, UUID> {

    Optional<DieticianInvite> findByToken(String token);

    @Query("SELECT di FROM DieticianInvite di WHERE " +
            "di.token = :token AND " +
            "di.usedAt IS NULL AND " +
            "di.expiresAt > :now")
    Optional<DieticianInvite> findValidInvite(@Param("token") String token,
                                              @Param("now") LocalDateTime now);

    @Query("SELECT di FROM DieticianInvite di WHERE " +
            "di.dietician.id = :dieticianId AND " +
            "di.usedAt IS NULL AND " +
            "di.expiresAt > :now")
    Optional<DieticianInvite> findPendingInviteByDieticianId(@Param("dieticianId") UUID dieticianId,
                                                             @Param("now") LocalDateTime now);

    List<DieticianInvite> findByExpiresAtBeforeAndUsedAtIsNull(LocalDateTime cutoff);
}