package com.learning.service.identity;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public interface OTPService {
    public String generateOTP();
    public Instant getExpiryTime();
    public boolean isValidFormat(String otp);

    @Transactional
    void deleteAllExpiredOtp();
}
