package com.learning.service.identity;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public interface OTPService {
    @Transactional
    String generateLoginOtp(Long userId, String deviceId);

    @Transactional
    boolean verifyLoginAttempt(Long userId, String otp);

    public boolean isValidFormat(String otp);

    @Transactional
    void deleteAllExpiredOtp();
}
