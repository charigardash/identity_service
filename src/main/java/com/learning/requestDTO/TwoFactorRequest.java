package com.learning.requestDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorRequest {
    private String code; // OTP or backup code
    private String deviceId;// Optional: for trusted devices
    private boolean rememberMe;// Optional: trust this device
}
