package com.learning.service.identity;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EmailService {
    public void sendOtp(String email, String otp);
    public void sendBackupCode(String email, List<String> backupCodes);
}
