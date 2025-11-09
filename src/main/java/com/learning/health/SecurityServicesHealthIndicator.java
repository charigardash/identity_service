package com.learning.health;

import com.learning.service.identity.RefreshTokenService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class SecurityServicesHealthIndicator implements HealthIndicator {
    private final RefreshTokenService refreshTokenService;

    public SecurityServicesHealthIndicator(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }
    @Override
    public Health health() {
        try {
            // Test refresh token service (if it has any critical operations)
            // For now, just check if the service is responsive
            refreshTokenService.deleteByAllExpiredToken();

            return Health.up()
                    .withDetail("service", "Security Services")
                    .withDetail("refresh_token_service", "Operational")
                    .withDetail("token_cleanup", "Working")
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("service", "Security Services")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
