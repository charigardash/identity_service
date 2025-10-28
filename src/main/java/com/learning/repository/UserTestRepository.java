package com.learning.repository;

import com.learning.dbentity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTestRepository extends JpaRepository<UserEntity, Long> {
}
