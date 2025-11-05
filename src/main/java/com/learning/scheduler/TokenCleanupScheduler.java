package com.learning.scheduler;


import com.learning.service.identity.OTPService;
import com.learning.service.identity.RefreshTokenService;
import com.learning.service.identity.serviceImp.OTPServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TokenCleanupScheduler {

    @Autowired
    private RefreshTokenService tokenService;

    @Autowired
    private OTPServiceImpl otpService;

    @Scheduled(cron = "0 0 3 * * ?") //Every day at 3 AM
    public void cleanUpExpiredToken(){
        tokenService.deleteByAllExpiredToken();
        otpService.deleteAllExpiredOtp();
    }
}
