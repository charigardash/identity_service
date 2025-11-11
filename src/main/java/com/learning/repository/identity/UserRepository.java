package com.learning.repository.identity;

import com.learning.dbentity.identity.User;
import com.learning.enums.OAuth2ProviderEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String userName);
    Optional<User> findByEmail(String email);
    Boolean existsByUserName(String userName);
    Boolean existsByEmail(String email);

    Optional<User> findByProviderAndProviderId(OAuth2ProviderEnum provider, String providerId);
}
