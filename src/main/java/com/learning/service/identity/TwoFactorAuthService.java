package com.learning.service.identity;

import com.learning.dbentity.identity.User2FA;
import com.learning.enums.TwoFAMethodEnum;
import com.learning.requestDTO.TwoFactorRequest;
import com.learning.responseDTO.TOTPSetupResponse;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public interface TwoFactorAuthService {
    public String authenticateUser2FA(Long userId, String deviceId, TwoFAMethodEnum method);

    @Transactional
    public User2FA enable2FactorAuth(Long userId);

    @Transactional
    public void disableTwoFactorAuth(Long userId);

    boolean isDeviceTrusted(Long userId, String deviceId);

    @Transactional
    boolean useBackupCode(Long userId, String backupCode);

    @Transactional
    List<String> regenrateBackupCodes(Long userId);

    Boolean twoFactorAuthStatus(Long userId);

    boolean verify2FACode(Long usedId, @NotBlank String code, TwoFAMethodEnum twoFactorRequestMethod, TwoFactorRequest twoFactorRequest);

    public Map<TwoFAMethodEnum, Boolean> getAvailable2FAMethods(Long userId);

    @Transactional
    TOTPSetupResponse setupTotp(Long userId);

    void verifyTOTPSetup(Long userId, String code);

    boolean isTotpSetup(Long userId);

    void disableTotp(Long userId);
}
