package com.learning.responseDTO;

import com.learning.enums.TwoFAMethodEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorResponse {
    private boolean requires2fa;
    private String message;
    private String tempToken; // Temporary token for 2FA verification
    private Long userId;
    //TODO : can enable methods basis of premium users
    private Map<TwoFAMethodEnum, Boolean> availableMethods;
}
