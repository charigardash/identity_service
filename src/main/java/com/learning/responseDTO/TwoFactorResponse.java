package com.learning.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorResponse {
    private boolean requires2fa;
    private String message;
    private String tempToken; // Temporary token for 2FA verification
    private Long userId;
}
