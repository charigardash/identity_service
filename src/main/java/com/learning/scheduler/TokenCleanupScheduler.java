package com.learning.scheduler;


import com.learning.service.identity.RefreshTokenService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TokenCleanupScheduler {

    private RefreshTokenService tokenService;

    @Scheduled(cron = "0 0 3 * * ?") //Every day at 3 AM
    public void cleanUpExpiredToken(){
        tokenService.deleteByAllExpiredToken();
    }
}
