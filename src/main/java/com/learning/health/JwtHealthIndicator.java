package com.learning.health;

import com.learning.dbentity.identity.User;
import com.learning.security.JwtUtils;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class JwtHealthIndicator implements HealthIndicator {

    private final JwtUtils jwtUtils;

    public JwtHealthIndicator(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Health health() {

        try {

            // Test JWT token generation and validation
            String testToken = jwtUtils.generateTokenFromUsername("health-check");
            String username = jwtUtils.getUsernameFromJwtToken(testToken);
            boolean isValid = jwtUtils.validateJwtToken(testToken);
            if (!"health-check".equals(username) || !isValid) {
                return Health.down()
                        .withDetail("service", "JWT Service")
                        .withDetail("error", "Token validation failed")
                        .build();
            }
            return Health.up()
                    .withDetail("service", "JWT Service")
                    .withDetail("status", "Operational")
                    .withDetail("token_generation", "Working")
                    .withDetail("token_validation", "Working")
                    .build();

        }catch (Exception e){
            return Health.down()
                    .withDetail("service", "JWT Service")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
