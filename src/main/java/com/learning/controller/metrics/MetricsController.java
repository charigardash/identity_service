package com.learning.controller.metrics;

import com.learning.health.SecurityMetricsService;
import com.learning.responseDTO.SecurityMetricsSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/identity/metrics")
public class MetricsController {
    @Autowired
    private SecurityMetricsService metricsService;

    /**
     * Get security metrics snapshot
     */
    @GetMapping("/security")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SecurityMetricsSnapshot> getSecurityMetrics() {
        SecurityMetricsSnapshot snapshot = metricsService.getMetricsSnapshot();
        return ResponseEntity.ok(snapshot);
    }

    /**
     * Get specific metric by name
     */
    @GetMapping("/security/{metricName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getSecurityMetric(@PathVariable String metricName) {
        SecurityMetricsSnapshot snapshot = metricsService.getMetricsSnapshot();

        switch (metricName.toLowerCase()) {
            case "logins":
                return ResponseEntity.ok().body(
                        new LoginMetrics(
                                snapshot.getLoginAttempts(),
                                snapshot.getLoginSuccess(),
                                snapshot.getLoginFailures(),
                                snapshot.getLoginSuccessRate()
                        )
                );

            case "twofactor":
                return ResponseEntity.ok().body(
                        new TwoFactorMetrics(
                                snapshot.getTwoFactorAttempts(),
                                snapshot.getTwoFactorSuccess(),
                                snapshot.getTwoFactorFailures(),
                                snapshot.getTwoFactorSuccessRate()
                        )
                );

            case "tokens":
                return ResponseEntity.ok().body(
                        new TokenMetrics(
                                snapshot.getTokenRefreshes(),
                                snapshot.getTokenRefreshFailures(),
                                snapshot.getTokenRefreshSuccessRate()
                        )
                );

            case "activity":
                return ResponseEntity.ok().body(
                        new ActivityMetrics(
                                snapshot.getActiveUsers(),
                                snapshot.getActiveSessions(),
                                snapshot.getUserRegistrations()
                        )
                );

            default:
                return ResponseEntity.badRequest().body("Unknown metric: " + metricName);
        }
    }

    // DTOs for specific metrics
    record LoginMetrics(double attempts, double success, double failures, double successRate) {}
    record TwoFactorMetrics(double attempts, double success, double failures, double successRate) {}
    record TokenMetrics(double refreshes, double failures, double successRate) {}
    record ActivityMetrics(int activeUsers, int activeSessions, double registrations) {}
}
