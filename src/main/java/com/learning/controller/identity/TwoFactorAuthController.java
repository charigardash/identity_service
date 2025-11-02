package com.learning.controller.identity;

import com.learning.service.identity.TwoFactorAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/identity/2fa")
public class TwoFactorAuthController {
    @Autowired
    private TwoFactorAuthService twoFactorAuthService;

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
