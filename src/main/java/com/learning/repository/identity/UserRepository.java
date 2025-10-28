package com.learning.repository.identity;

import com.learning.dbentity.identity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String userName);
    Optional<User> findByEmail(String email);
    Boolean existsByUserName(String userName);
    Boolean existsByEmail(String email);
}
