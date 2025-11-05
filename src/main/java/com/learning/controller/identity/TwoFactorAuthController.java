package com.learning.controller.identity;

import com.learning.enums.TwoFAMethodEnum;
import com.learning.responseDTO.TOTPSetupResponse;
import com.learning.service.identity.TwoFactorAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/identity/2fa")
public class TwoFactorAuthController {
    @Autowired
    private TwoFactorAuthService twoFactorAuthService;

    /**
     * Setup TOTP for authenticator app
     */
    @PostMapping("/totp/setup")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> enableTotp(@RequestParam Long userId){
        TOTPSetupResponse response = twoFactorAuthService.setupTotp(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify TOTP setup (confirm user can generate codes)
     */
    @PostMapping("/totp/verify-setup")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    ResponseEntity<?> verifyTOTPSetup(@RequestParam Long userId, @RequestParam String code){
        twoFactorAuthService.verifyTOTPSetup(userId, code);
        return ResponseEntity.ok("TOTP setup verified successfully");
    }

    /**
     * Get available 2FA methods for user
     */
    @GetMapping("/methods")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getAvailableMethods(@RequestParam Long userId){
        Map<TwoFAMethodEnum, Boolean> available2FAMethods = twoFactorAuthService.getAvailable2FAMethods(userId);
        return ResponseEntity.ok(available2FAMethods);
    }

    /**
     * Check if TOTP is setup
     */
    @GetMapping("/totp/status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getTOTPStatus(@RequestParam Long userId){
        boolean isSetup = twoFactorAuthService.isTotpSetup(userId);
        return ResponseEntity.ok(Collections.singletonMap("totpSetup", isSetup));
    }

    /**
     * Disable TOTP (remove authenticator app)
     */
    @PostMapping("/totp/disable")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> disableTotp(@RequestParam Long userId){
        twoFactorAuthService.disableTotp(userId);
        return ResponseEntity.ok("TOTP disabled successfully");
    }

    @PostMapping("/enable")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> enableTwoFactorAuth(@RequestParam Long userId){
        try {
            return ResponseEntity.ok(twoFactorAuthService.enable2FactorAuth(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/disable")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> disableTwoFactorAuth(@RequestParam Long userId){
        twoFactorAuthService.disableTwoFactorAuth(userId);
        return ResponseEntity.ok("Two-factor authentication disabled successfully");
    }

    @PostMapping("/regenrate-backup-codes/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> regenrateBackupCodes(@PathVariable Long userId){
        return ResponseEntity.ok(twoFactorAuthService.regenrateBackupCodes(userId));
    }

    @PostMapping("/status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getTwoFactorStatus(@RequestParam Long userId){
        return ResponseEntity.ok(twoFactorAuthService.twoFactorAuthStatus(userId));
    }
}
