package com.learning.service.identity.serviceImp;

import com.learning.customException.IdentityAppExcpetion;
import com.learning.customException.UserNotFoundException;
import com.learning.dbentity.identity.OTPCode;
import com.learning.dbentity.identity.TrustedDevice;
import com.learning.dbentity.identity.User;
import com.learning.dbentity.identity.User2FA;
import com.learning.enums.DeviceNameEnum;
import com.learning.enums.OtpTypeEnum;
import com.learning.repository.identity.OTPCodeRepository;
import com.learning.repository.identity.TrustedDeviceRepository;
import com.learning.repository.identity.User2FARepository;
import com.learning.repository.identity.UserRepository;
import com.learning.service.identity.EmailService;
import com.learning.service.identity.OTPService;
import com.learning.service.identity.TwoFactorAuthService;
import com.learning.utility.identity.TwoFactorAuthUtil;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

import static com.learning.utility.identity.TwoFactorAuthUtil.generateSecretKey;

@Service
public class TwoFactorAuthServiceImpl implements TwoFactorAuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private User2FARepository user2FARepository;

    @Autowired
    private TrustedDeviceRepository trustedDeviceRepository;

    @Autowired
    private OTPCodeRepository otpCodeRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OTPService otpService;

    @Value("${app.2fa.max-attempts-per-hour:5}")
    private long maxAttemptsPerHour;



    /**
     * Enable 2FA for a user
     */
    @Override
    @Transactional
    public User2FA enable2FactorAuth(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        User2FA user2FA = user2FARepository.findByUser(user).orElse(new User2FA());
        // Generate secret key for TOTP (optional - for authenticator apps)
        user2FA.setSecretKey(generateSecretKey());
        List<String> backupCodes = TwoFactorAuthUtil.generateBackupCodes();
        user2FA.setBackupCodes(TwoFactorAuthUtil.convertBackupCodesToJson(backupCodes));
        user2FA.setEnabled(true);
        User2FA saved = user2FARepository.save(user2FA);
        //send backup codes to user email for fall back mechanism if user lost his/her secret key
        emailService.sendBackupCode(user.getEmail(), backupCodes);
        return saved;
    }

    /**
     * Disable 2FA for a user
     */

    @Override
    @Transactional
    public void disableTwoFactorAuth(Long userId){

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        User2FA user2FA = user2FARepository.findByUser(user).orElseThrow(() -> new IdentityAppExcpetion("User 2FA is not enabled", HttpStatus.BAD_REQUEST));
        user2FA.setEnabled(false);
        user2FA.setSecretKey(null);
        user2FA.setBackupCodes(null);
        user2FARepository.save(user2FA);

        trustedDeviceRepository.deleteAll(trustedDeviceRepository.findByUser(user));
    }

    /**
     * Generate and send otp for login
     * @return otp
     */
    @Override
    @Transactional
    public String generateLoginOtp(Long userId, String deviceId){
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException(userId));
        //check rate limit
        checkRateLimit(user);
        String otp = otpService.generateOTP();
        Instant expiresAt = otpService.getExpiryTime();
        //mark all previous unused otp's expired
        otpCodeRepository.markAllAsUsed(user, OtpTypeEnum.LOGIN);
        OTPCode otpCode = new OTPCode(user, otp, OtpTypeEnum.LOGIN, expiresAt);
        otpCodeRepository.save(otpCode);
        // TODO: send otp via sms
        emailService.sendOtp(user.getEmail(), otp);
        return otp;
    }

    /**
     * Verify OTP for login
     */

    @Override
    @Transactional
    public boolean verifyLoginAttempt(Long userId, String otp, String deviceId){
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Optional<OTPCode> optionalOTPCode = otpCodeRepository.findByCodeAndTypeAndUsedFalse(otp, OtpTypeEnum.LOGIN);
        if(optionalOTPCode.isEmpty())return false;
        OTPCode otpCode = optionalOTPCode.get();
        if(Objects.equals(otpCode.getUser().getId(), userId)){
            return false;
        }
        if(otpCode.getExpiresAt().isBefore(Instant.now())){
            return false;
        }
        otpCode.setUsed(true);
        otpCodeRepository.save(otpCode);
        //If device is not trusted add it to trusted list
        if(StringUtils.isNotEmpty(deviceId) && !trustedDeviceRepository.existsByUserAndDeviceId(user, deviceId)){
            TrustedDevice trustedDevice = new TrustedDevice(user, deviceId, DeviceNameEnum.WEB_BROWSER.getDeviceName());
            trustedDeviceRepository.save(trustedDevice);
        }
        return true;
    }

    /**
     * Check if device is trusted (skip 2FA)
     */

    @Override
    public boolean isDeviceTrusted(Long userId, String deviceId){
        if(StringUtils.isEmpty(deviceId))return false;
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return trustedDeviceRepository.existsByUserAndDeviceId(user, deviceId);
    }

    /**
     * Use backup code for login
     * @param userId
     * @param backupCode
     * @return
     */

    @Transactional
    @Override
    public boolean useBackupCode(Long userId, String backupCode){
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        User2FA user2FA = user2FARepository.findByUser(user).orElseThrow(() -> new IdentityAppExcpetion("2FA not enabled", HttpStatus.BAD_REQUEST));
        List<String> backupCodes = TwoFactorAuthUtil.parseBackupCodes(user2FA.getBackupCodes());
        if(backupCodes.contains(backupCode)){
            backupCodes.remove(backupCode);
            user2FA.setBackupCodes(TwoFactorAuthUtil.convertBackupCodesToJson(backupCodes));
            user2FARepository.save(user2FA);
            return true;
        }
        return false;
    }

    @Transactional
    @Override
    public List<String> regenrateBackupCodes(Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        User2FA user2FA = user2FARepository.findByUser(user).orElseThrow(() -> new IdentityAppExcpetion("2FA not enabled", HttpStatus.BAD_REQUEST));
        List<String> backupCodes = TwoFactorAuthUtil.generateBackupCodes();
        user2FA.setBackupCodes(TwoFactorAuthUtil.convertBackupCodesToJson(backupCodes));
        user2FARepository.save(user2FA);
        emailService.sendBackupCode(user.getEmail(), backupCodes);
        return backupCodes;
    }

    @Override
    public Boolean twoFactorAuthStatus(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return user2FARepository.findByUser(user).map(User2FA::getEnabled).orElse(false);
    }

    private void checkRateLimit(User user){
        Instant oneHourAgo = Instant.now().minusSeconds(3600);
        long recentAttempts = otpCodeRepository.countRecentAttempts(user, oneHourAgo);
        if(maxAttemptsPerHour < recentAttempts){
            throw new IdentityAppExcpetion("Too many otp attempts, please try again later", HttpStatus.FORBIDDEN);
        }
    }

}
