package com.learning.responseDTO;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SecurityMetricsSnapshot {
    private double loginAttempts;
    private double loginSuccess;
    private double loginFailures;
    private double twoFactorAttempts;
    private double twoFactorSuccess;
    private double twoFactorFailures;
    private double tokenRefreshes;
    private double tokenRefreshFailures;
    private double userRegistrations;
    private int activeSessions;
    private int activeUsers;

    // Calculated properties
    public double getLoginSuccessRate() {
        return loginAttempts > 0 ? (loginSuccess / loginAttempts) * 100 : 0;
    }

    public double getTwoFactorSuccessRate() {
        return twoFactorAttempts > 0 ? (twoFactorSuccess / twoFactorAttempts) * 100 : 0;
    }

    public double getTokenRefreshSuccessRate() {
        return tokenRefreshes > 0 ? ((tokenRefreshes - tokenRefreshFailures) / tokenRefreshes) * 100 : 0;
    }
}
