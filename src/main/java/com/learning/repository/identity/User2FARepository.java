package com.learning.repository.identity;

import com.learning.dbentity.identity.User;
import com.learning.dbentity.identity.User2FA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface User2FARepository extends JpaRepository<User2FA, Long> {
    Optional<User2FA> findByUser(User user);
    Optional<User2FA> findByUserId(Long userId);
    boolean existsByUserAndEnabledTrue(User user);
}
