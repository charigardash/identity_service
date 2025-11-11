package com.learning.controller.identity;

import com.learning.security.UserPrincipal;
import com.learning.service.identity.AuthService;
import com.learning.service.social.OAuth2Service;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/identity/social")
public class SocialLoginController {

    @Autowired
    private AuthService authService;

    @Autowired
    private OAuth2Service oAuth2Service;

    /**
     * Link social account to existing local account
     */
    @PostMapping("/link")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> linkSocialAccount(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                               @RequestParam String provider,
                                               @RequestParam String providerId){
        oAuth2Service.linkSocialAccount(userPrincipal, provider, providerId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Social account linked successfully");
        response.put("provider", provider);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Unlink social account
     */
    @PostMapping("/unlink")
    @PreAuthorize(("hasRole('USER') or hasRole('ADMIN')"))
    public ResponseEntity<?> unlinkSocialAccount(@AuthenticationPrincipal UserPrincipal userPrincipal){
        oAuth2Service.unlinkSocialAccount(userPrincipal);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Social account unlinked successfully");
        response.put("provider", "local");

        return ResponseEntity.ok(response);
    }

    /**
     * Get user's social connections
     */
    @GetMapping("/connections")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getSocialConnections(@AuthenticationPrincipal UserPrincipal userPrincipal){
        Map<String, Object> response = oAuth2Service.getSocialConnectionForUser(userPrincipal);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Check if email exists for social login
     */
    @GetMapping("/check-email")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> checkEmailExists(@RequestParam String email){
        Map<String, Object> response = oAuth2Service.checkUserExistsUsingEmail(email);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Set password for social login user (who doesn't have a password)
     */
    @PostMapping("/set-password")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> setPassword(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                         @Valid String request) {
        try {
            String result = authService.setPassword(userPrincipal.getId(), request);
            return ResponseEntity.ok(Collections.singletonMap("message", result));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
