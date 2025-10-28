package com.learning.requestDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class LoginRequest {
    @NotBlank
    private String userName;
    @NotBlank
    private String password;
}
