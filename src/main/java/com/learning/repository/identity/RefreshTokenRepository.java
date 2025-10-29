package com.learning.repository.identity;

import com.learning.dbentity.identity.RefreshToken;
import com.learning.dbentity.identity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String refreshToken);

    List<RefreshToken> findByUser(User user);

    @Modifying
    @Query("DELETE FROM RefreshToken rt where rt.expiryDate < CURRENT_TIMESTAMP")
    void deleteAllExpiredToken();

    @Modifying
    void deleteByUser(User user);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user")
    void revokeAllUserTokens(User user);
}
