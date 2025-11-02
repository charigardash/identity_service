package com.learning.service.identity;

import com.learning.dbentity.identity.User2FA;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public interface TwoFactorAuthService {
    @Transactional
    public User2FA enable2FactorAuth(Long userId);

    @Transactional
    public void disableTwoFactorAuth(Long userId);

    @Transactional
    public String generateLoginOtp(Long userId, String deviceId);

    @Transactional
    public boolean verifyLoginAttempt(Long userId, String otp, String deviceId);

    boolean isDeviceTrusted(Long userId, String deviceId);

    @Transactional
    boolean useBackupCode(Long userId, String backupCode);

    @Transactional
    List<String> regenrateBackupCodes(Long userId);

    Boolean twoFactorAuthStatus(Long userId);
}
