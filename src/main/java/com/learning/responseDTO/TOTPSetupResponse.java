package com.learning.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TOTPSetupResponse implements Serializable {
    private String secretKey;
    private String qrCodeUrl;
    private String qrCodeBase64;
    private String manualEntryKey;
    private String message;
}
