package com.learning.service.identity.serviceImp;

import com.learning.customException.IdentityAppExcpetion;
import com.learning.customException.UserNotFoundException;
import com.learning.dbentity.identity.OTPCode;
import com.learning.dbentity.identity.TrustedDevice;
import com.learning.dbentity.identity.User;
import com.learning.enums.DeviceNameEnum;
import com.learning.enums.OtpTypeEnum;
import com.learning.repository.identity.OTPCodeRepository;
import com.learning.repository.identity.TrustedDeviceRepository;
import com.learning.repository.identity.UserRepository;
import com.learning.service.identity.EmailService;
import com.learning.service.identity.OTPService;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service("OTP")
public class OTPServiceImpl implements OTPService {
    private static final String NUMBERS = "0123456789";
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPT_PER_HOUR = 5;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.2fa.max-attempts-per-hour:5}")
    private long maxAttemptsPerHour;

    @Autowired
    private OTPCodeRepository otpCodeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TrustedDeviceRepository trustedDeviceRepository;


    /**
     * Generate and send otp for login
     * @return otp
     */
    @Transactional
    @Override
    public String generateLoginOtp(Long userId, String deviceId){
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException(userId));
        //check rate limit
        checkRateLimit(user);
        String otp = generateOTP();
        Instant expiresAt = getExpiryTime();
        //mark all previous unused otp's expired
        otpCodeRepository.markAllAsUsed(user, OtpTypeEnum.LOGIN);
        OTPCode otpCode = new OTPCode(user, otp, OtpTypeEnum.LOGIN, expiresAt);
        otpCodeRepository.save(otpCode);
        // TODO: send otp via sms
        emailService.sendOtp(user.getEmail(), otp);
        return "OTP sent to your email.";
    }


    /**
     * Verify OTP for login
     */
    @Transactional
    @Override
    public boolean verifyLoginAttempt(Long userId, String otp){
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Optional<OTPCode> optionalOTPCode = otpCodeRepository.findByCodeAndTypeAndUsedFalse(otp, OtpTypeEnum.LOGIN);
        if(optionalOTPCode.isEmpty())return false;
        OTPCode otpCode = optionalOTPCode.get();
        if(!Objects.equals(otpCode.getUser().getId(), userId)){
            return false;
        }
        if(otpCode.getExpiresAt().isBefore(Instant.now())){
            return false;
        }
        otpCode.setUsed(true);
        otpCodeRepository.save(otpCode);
//        //If device is not trusted add it to trusted list
//        if(StringUtils.isNotEmpty(deviceId) && !trustedDeviceRepository.existsByUserAndDeviceId(user, deviceId)){
//            TrustedDevice trustedDevice = new TrustedDevice(user, deviceId, DeviceNameEnum.WEB_BROWSER.getDeviceName());
//            trustedDeviceRepository.save(trustedDevice);
//        }
        return true;
    }

    private String generateOTP() {
        StringBuilder otp = new StringBuilder();
        for(int i=0;i<OTP_LENGTH; i++){
            otp.append(NUMBERS.charAt(secureRandom.nextInt(NUMBERS.length())));
        }
        return otp.toString();
    }

    private Instant getExpiryTime() {
        return Instant.now().plus(OTP_EXPIRY_MINUTES, TimeUnit.MINUTES.toChronoUnit());
    }

    @Override
    public boolean isValidFormat(String otp) {
        return otp != null && otp.length() == OTP_LENGTH && otp.chars().allMatch(Character::isDigit);
    }

    @Transactional
    @Override
    public void deleteAllExpiredOtp(){
        otpCodeRepository.deleteAllUsedOtp();
    }

    private void checkRateLimit(User user){
        Instant oneHourAgo = Instant.now().minusSeconds(3600);
        long recentAttempts = otpCodeRepository.countRecentAttempts(user, oneHourAgo);
        if(maxAttemptsPerHour < recentAttempts){
            throw new IdentityAppExcpetion("Too many otp attempts, please try again later", HttpStatus.FORBIDDEN);
        }
    }
}
