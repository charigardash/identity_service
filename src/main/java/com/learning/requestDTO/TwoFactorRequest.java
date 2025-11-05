package com.learning.requestDTO;

import com.learning.enums.TwoFAMethodEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorRequest {
    @NotBlank
    private String code; // OTP or backup code
    private String deviceId;// Optional: for trusted devices
    private boolean rememberDevice;// Optional: trust this device
    private TwoFAMethodEnum method;
}
