package com.learning.service.identity;

import org.springframework.stereotype.Service;

@Service
public interface QrCodeService {

    /**
     * Generate QR code for TOTP setup
     */
    public String generateTOTPQRCode(String appName, String username, String secretKey);
}
