package com.learning.service.identity;

import com.learning.dbentity.identity.User;
import com.learning.requestDTO.LoginRequest;
import com.learning.requestDTO.SignupRequest;
import com.learning.requestDTO.TokenRefreshRequest;
import com.learning.responseDTO.JwtResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AuthService {
    String registerUser(SignupRequest signupRequest);

    List<User> getAllRegisterUser();

    JwtResponse authenticateUser(LoginRequest loginRequest);

    JwtResponse refreshJwtToken(TokenRefreshRequest refreshRequest);

    void logoutUser(TokenRefreshRequest refreshRequest);

    void logoutAllUser(Long userId);
}
