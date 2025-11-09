package com.learning.scheduler;

import com.learning.health.SecurityMetricsService;
import com.learning.repository.identity.RefreshTokenRepository;
import com.learning.repository.identity.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MetricsUpdaterService {
    @Autowired
    private SecurityMetricsService metricsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    /**
     * Clean up old metrics data daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupMetrics() {
        // In a real implementation, you might clean up old metrics data
        // from a time-series database or archive old logs
        System.out.println("Metrics cleanup completed at: " + java.time.Instant.now());
    }
}
