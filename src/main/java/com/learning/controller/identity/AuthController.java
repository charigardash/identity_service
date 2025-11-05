package com.learning.controller.identity;

import com.learning.dbentity.identity.User;
import com.learning.requestDTO.LoginRequest;
import com.learning.requestDTO.SignupRequest;
import com.learning.requestDTO.TokenRefreshRequest;
import com.learning.requestDTO.TwoFactorRequest;
import com.learning.responseDTO.JwtResponse;
import com.learning.responseDTO.TwoFactorResponse;
import com.learning.service.identity.AuthService;
import com.learning.service.identity.TwoFactorAuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private TwoFactorAuthService twoFactorAuthService;

//    @Autowired
//    public AuthController(AuthService authService, TwoFactorAuthService twoFactorAuthService){
//        this.authService = authService;
//        this.twoFactorAuthService = twoFactorAuthService;
//    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUpRequest(@Valid @RequestBody SignupRequest signupRequest){
        String signupResponse = authService.registerUser(signupRequest);
        return ResponseEntity.ok(signupResponse);
    }

    @PostMapping("/signing")
    public ResponseEntity<?> signingRequest(@Valid @RequestBody LoginRequest loginRequest,
                                            @RequestHeader(value = "X-Device-Id", required = false) String deviceId){
        Object response = authService.authenticateUser(loginRequest, deviceId);
        if(response instanceof JwtResponse){
            return ResponseEntity.ok((response));
        }else if(response instanceof TwoFactorResponse twoFactorResponse){
            twoFactorResponse.setAvailableMethods(twoFactorAuthService.getAvailable2FAMethods(twoFactorResponse.getUserId()));
            return ResponseEntity.ok(twoFactorResponse);
        }else {
            return ResponseEntity.badRequest().body("Unexpected response type");
        }
    }

    /**
     * verify the otp/totp
     * @param userId
     * @param twoFactorRequest
     * @return
     */
    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verifyTwoFactor(@RequestParam Long userId,@Valid @RequestBody TwoFactorRequest twoFactorRequest){
        try {
            JwtResponse jwtResponse = authService.verifyTwoFactor(userId, twoFactorRequest);
            return ResponseEntity.ok(jwtResponse);
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> fetchAllRegisterUser(){
        List<User> allRegisterUser = authService.getAllRegisterUser();
        return ResponseEntity.ok(allRegisterUser);
    }

    /**
     * generate jwt token from refresh token
     * @param request
     * @return
     */
    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request){
        JwtResponse jwtResponse = authService.refreshJwtToken(request);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@Valid @RequestBody TokenRefreshRequest refreshRequest){
        authService.logoutUser(refreshRequest);
        return ResponseEntity.ok("Log out successful!");
    }

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAllFromAllDevices(@RequestParam Long userId){
        authService.logoutAllUser(userId);
        return ResponseEntity.ok("Logged out from all devices!");
    }
}
