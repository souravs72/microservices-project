package com.microservices.auth.repository;

import com.microservices.auth.entity.PasswordResetToken;
import com.microservices.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    @Modifying
    @Query("DELETE FROM PasswordResetToken prt WHERE prt.expiryDate < :now OR prt.used = true")
    void deleteExpiredAndUsedTokens(LocalDateTime now);

    @Modifying
    @Query("UPDATE PasswordResetToken prt SET prt.used = true WHERE prt.user = :user AND prt.used = false")
    void invalidateAllUserTokens(User user);
}