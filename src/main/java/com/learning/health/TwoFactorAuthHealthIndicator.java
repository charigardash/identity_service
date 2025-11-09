package com.learning.health;

import ch.qos.logback.core.util.StringUtil;
import com.learning.service.identity.serviceImp.TotpServiceImpl;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class TwoFactorAuthHealthIndicator implements HealthIndicator {

    private final TotpServiceImpl totpService;

    public TwoFactorAuthHealthIndicator(TotpServiceImpl totpService) {
        this.totpService = totpService;
    }
    @Override
    public Health health() {
        try{
            // Test TOTP generation
            String secretKey = "TESTKEY1234567890";
            String code = totpService.generateTOTP(secretKey, System.currentTimeMillis()/30000);
            if (StringUtil.isNullOrEmpty(code)) {
                return Health.down()
                        .withDetail("service", "Two-Factor Authentication")
                        .withDetail("error", "TOTP validation failed")
                        .build();
            }
            return Health.up()
                    .withDetail("service", "Two-Factor Authentication")
                    .withDetail("status", "Operational")
                    .withDetail("totp_generation", "Working")
                    .build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
