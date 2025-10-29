package com.learning.controller.identity;

import com.learning.dbentity.identity.User;
import com.learning.requestDTO.LoginRequest;
import com.learning.requestDTO.SignupRequest;
import com.learning.requestDTO.TokenRefreshRequest;
import com.learning.responseDTO.JwtResponse;
import com.learning.service.identity.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    private final AuthService authService;

    AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUpRequest(@Valid @RequestBody SignupRequest signupRequest){
        String signupResponse = authService.registerUser(signupRequest);
        return ResponseEntity.ok(signupResponse);
    }

    @PostMapping("/signing")
    public ResponseEntity<?> signingRequest(@Valid @RequestBody LoginRequest loginRequest){
        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @GetMapping("/users")
    public ResponseEntity<?> fetchAllRegisterUser(){
        List<User> allRegisterUser = authService.getAllRegisterUser();
        return ResponseEntity.ok(allRegisterUser);
    }

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
