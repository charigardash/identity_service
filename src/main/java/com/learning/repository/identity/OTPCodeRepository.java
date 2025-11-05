package com.learning.repository.identity;

import com.learning.dbentity.identity.OTPCode;
import com.learning.dbentity.identity.User;
import com.learning.enums.OtpTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OTPCodeRepository extends JpaRepository<OTPCode, Long> {

    Optional<OTPCode> findByCodeAndTypeAndUsedFalse(String code, OtpTypeEnum type);

    @Modifying
    @Query("DELETE FROM OTPCode O WHERE O.expiresAt < :now")
    void deleteAllExpired(Instant now);

    @Modifying
    @Query("UPDATE OTPCode o SET o.used = true where o.user =:user and o.type = :type")
    void markAllAsUsed(User user, OtpTypeEnum type);

    @Query("SELECT COUNT(o) FROM OTPCode o WHERE o.user = :user AND o.createdAt > :since")
    long countRecentAttempts(User user, Instant since);

    @Modifying
    @Query("DELETE FROM OTPCode o where o.expiresAt < CURRENT_TIMESTAMP")
    void deleteAllUsedOtp();
}
