package com.learning.requestDTO;

import com.learning.enums.TwoFAMethodEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class LoginRequest {
    @NotBlank
    private String userName;
    @NotBlank
    private String password;

    private TwoFAMethodEnum method = TwoFAMethodEnum.OTP;
}
