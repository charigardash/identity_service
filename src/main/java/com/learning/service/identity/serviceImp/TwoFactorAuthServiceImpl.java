package com.learning.service.identity.serviceImp;

import com.learning.customException.IdentityAppExcpetion;
import com.learning.customException.UserNotFoundException;
import com.learning.dbentity.identity.TrustedDevice;
import com.learning.dbentity.identity.User;
import com.learning.dbentity.identity.User2FA;
import com.learning.enums.DeviceNameEnum;
import com.learning.enums.TwoFAMethodEnum;
import com.learning.repository.identity.OTPCodeRepository;
import com.learning.repository.identity.TrustedDeviceRepository;
import com.learning.repository.identity.User2FARepository;
import com.learning.repository.identity.UserRepository;
import com.learning.requestDTO.TwoFactorRequest;
import com.learning.responseDTO.TOTPSetupResponse;
import com.learning.service.identity.EmailService;
import com.learning.service.identity.OTPService;
import com.learning.service.identity.QrCodeService;
import com.learning.service.identity.TwoFactorAuthService;
import com.learning.utility.identity.TwoFactorAuthUtil;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.learning.utility.identity.TwoFactorAuthUtil.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


// TODO : check the algorithm
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
    private TotpServiceImpl totpService;

    @Autowired
    private QrCodeService qrCodeService;


    private final Map<String, OTPService> factoryMap;

    @Autowired
    public TwoFactorAuthServiceImpl(Map<String, OTPService> factoryMap) {
        this.factoryMap = factoryMap;
    }


    /**
     * Setup TOTP for a user (for authenticator apps)
     */
    @Transactional
    @Override
    public TOTPSetupResponse setupTotp(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException(userId));
        User2FA user2FA = user2FARepository.findByUser(user).orElse(new User2FA(user));
        String secretKey = totpService.generateSecretKey();
        user2FA.setEnabled(true);
        user2FA.setSecretKey(secretKey);
        user2FARepository.save(user2FA);
        // Generate QR code for authenticator app
        String qrCodeUrlForAuthenticatorApp = totpService.generateQRCodeURL("Identity", user.getUserName(), secretKey);
        String qrCodeBase64 = qrCodeService.generateTOTPQRCode("Identity", user.getUserName(), secretKey);
        String manualEntryKey = formatSecretKeyForDisplay(secretKey);
        return TOTPSetupResponse.builder()
                .qrCodeUrl(qrCodeUrlForAuthenticatorApp)
                .manualEntryKey(manualEntryKey)
                .secretKey(secretKey)
                .qrCodeBase64(qrCodeBase64)
                .message("Scan the QR code with your authenticator app or enter the key manually")
                .build();
    }

    /**
     * Verify TOTP setup by confirming the user can generate correct codes
     * @param userId
     * @param code
     */
    @Override
    public void verifyTOTPSetup(Long userId, String code) {
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException(userId));
        User2FA user2FA = user2FARepository.findByUser(user).orElseThrow(()-> new IdentityAppExcpetion("2FA not enabled for user", HttpStatus.BAD_REQUEST));
        String secretKey = user2FA.getSecretKey();
        if(secretKey == null){
            throw new IdentityAppExcpetion("TOTP secret key not found", HttpStatus.BAD_REQUEST);
        }
        // Verify the code using TOTP service
        boolean verified = totpService.verifyLoginAttempt(userId, code);
        if(verified){
            List<String> backupCodes = generateBackupCodes();
            user2FA.setBackupCodes(convertBackupCodesToJson(backupCodes));
            user2FARepository.save(user2FA);
            emailService.sendBackupCode(user.getEmail(), backupCodes);
        }else {
            throw new IdentityAppExcpetion("Invalid verification code", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public boolean isTotpSetup(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException(userId));
        return user2FARepository.findByUser(user)
                .map(user2FA -> user2FA.getSecretKey() != null)
                .orElse(false);
    }

    @Override
    public void disableTotp(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException(userId));
        User2FA user2FA = user2FARepository.findByUser(user).orElseThrow(()-> new IdentityAppExcpetion("2FA not enabled for user", HttpStatus.BAD_REQUEST));
        user2FA.setSecretKey(null);
        user2FARepository.save(user2FA);
    }

    @Override
    public String authenticateUser2FA(Long userId, String deviceId, TwoFAMethodEnum method){

        OTPService otpService = factoryMap.getOrDefault(method.getMethod(), factoryMap.get("TOTP"));
        return otpService.generateLoginOtp(userId, deviceId);
    }

    /**
     * Enhanced verification that supports both TOTP and OTP
     */
    @Override
    public boolean verify2FACode(Long usedId, String code, TwoFAMethodEnum twoFactorRequestMethod, TwoFactorRequest twoFactorRequest) {
        User user = userRepository.findById(usedId).orElseThrow(()-> new UserNotFoundException(usedId));
        User2FA user2FA = user2FARepository.findByUser(user).orElseThrow(()-> new IdentityAppExcpetion("2FA not enabled for user", HttpStatus.BAD_REQUEST));
        OTPService otpService = factoryMap.get(twoFactorRequestMethod.getMethod());
        boolean isValid = otpService.verifyLoginAttempt(usedId, code);
        if(isValid && StringUtils.isNotEmpty(twoFactorRequest.getDeviceId())){
            markDeviceAsTrusted(user, twoFactorRequest.getDeviceId());
        }
        return isValid;
    }

    @Override
    public Map<TwoFAMethodEnum, Boolean> getAvailable2FAMethods(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException(userId));
        User2FA user2FA = user2FARepository.findByUser(user).orElseThrow(()-> new IdentityAppExcpetion("2FA not enabled for user", HttpStatus.BAD_REQUEST));
        Map<TwoFAMethodEnum, Boolean> availableTwoFAMethods = new HashMap<>();
        availableTwoFAMethods.put(TwoFAMethodEnum.OTP, TRUE);// Email/SMS OTP is always available
        if(user2FA != null && user2FA.getEnabled()){
            availableTwoFAMethods.put(TwoFAMethodEnum.TOTP, StringUtils.isNotEmpty(user2FA.getSecretKey()));
            availableTwoFAMethods.put(TwoFAMethodEnum.BACKUP, user2FA.getBackupCodes() != null && !TwoFactorAuthUtil.parseBackupCodes(user2FA.getBackupCodes()).isEmpty());
        } else{
            availableTwoFAMethods.put(TwoFAMethodEnum.TOTP, FALSE);
            availableTwoFAMethods.put(TwoFAMethodEnum.BACKUP, FALSE);
        }
        return availableTwoFAMethods;
    }

    private void markDeviceAsTrusted(User user, String deviceId) {
        if(!trustedDeviceRepository.existsByUserAndDeviceId(user, deviceId)){
            TrustedDevice trustedDevice = new TrustedDevice(user, deviceId, DeviceNameEnum.WEB_BROWSER.getDeviceName());
            trustedDeviceRepository.save(trustedDevice);
        }
    }


    /**
     * Enable 2FA for a user
     */
    @Override
    @Transactional
    public User2FA enable2FactorAuth(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        User2FA user2FA = user2FARepository.findByUser(user).orElse(new User2FA(user));
        // Generate secret key for TOTP (optional - for authenticator apps)
//        user2FA.setSecretKey(generateSecretKey());
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

    // Helper methods
    private String formatSecretKeyForDisplay(String secretKey) {
        // Format as "AAAA BBBB CCCC DDDD EEEE FFFF GGGG HHHH"
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < secretKey.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(" ");
            }
            formatted.append(secretKey.charAt(i));
        }
        return formatted.toString();
    }

}
