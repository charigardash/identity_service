package com.learning.service.identity.serviceImp;

import com.learning.repository.identity.OTPCodeRepository;
import com.learning.service.identity.OTPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class OTPServiceImpl implements OTPService {
    private static final String NUMBERS = "0123456789";
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPT_PER_HOUR = 5;
    private final SecureRandom secureRandom = new SecureRandom();
    @Autowired
    private OTPCodeRepository otpCodeRepository;
    @Override
    public String generateOTP() {
        StringBuilder otp = new StringBuilder();
        for(int i=0;i<OTP_LENGTH; i++){
            otp.append(NUMBERS.charAt(secureRandom.nextInt(NUMBERS.length())));
        }
        return otp.toString();
    }

    @Override
    public Instant getExpiryTime() {
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
}
