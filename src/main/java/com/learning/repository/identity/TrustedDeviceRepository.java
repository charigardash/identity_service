package com.learning.repository.identity;

import com.learning.dbentity.identity.TrustedDevice;
import com.learning.dbentity.identity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrustedDeviceRepository extends JpaRepository<TrustedDevice, Long> {
    Optional<TrustedDevice> findByUserAndDeviceId(User user, String deviceId);
    List<TrustedDevice> findByUser(User user);
    boolean existsByUserAndDeviceId(User user, String deviceId);
    @Modifying
    void deleteByUserAndDeviceId(User user, String deviceId);
}
