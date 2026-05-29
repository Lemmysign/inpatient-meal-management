package com.hospital.meal.repository;

import com.hospital.meal.model.user.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    /**
     * Find valid token that hasn't expired or been used
     */
    @Query("SELECT prt FROM PasswordResetToken prt WHERE " +
            "prt.token = :token AND " +
            "prt.isValid = true AND " +
            "prt.usedAt IS NULL AND " +
            "prt.expiresAt > :now")
    Optional<PasswordResetToken> findValidToken(
            @Param("token") String token,
            @Param("now") LocalDateTime now
    );

    /**
     * Find by email and user type
     */
    Optional<PasswordResetToken> findByEmailAndUserType(String email, String userType);

    /**
     * Invalidate all tokens for a user
     */
    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.isValid = false " +
            "WHERE prt.email = :email AND prt.userType = :userType")
    void invalidateAllTokensForUser(
            @Param("email") String email,
            @Param("userType") String userType
    );

    /**
     * Delete expired tokens (cleanup)
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiresAt < :cutoffTime")
    void deleteExpiredTokens(@Param("cutoffTime") LocalDateTime cutoffTime);
}