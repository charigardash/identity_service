package com.learning.health;

import com.learning.responseDTO.SecurityMetricsSnapshot;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SecurityMetricsService {

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, Counter> counters;
    private final ConcurrentHashMap<String, Timer> timers;


    // Counters
    private Counter loginAttemptsCounter;
    private Counter loginSuccessCounter;
    private Counter loginFailureCounter;
    private Counter twoFactorAttemptsCounter;
    private Counter twoFactorSuccessCounter;
    private Counter twoFactorFailureCounter;
    private Counter tokenRefreshCounter;
    private Counter tokenRefreshFailureCounter;
    private Counter userRegistrationCounter;

    // Gauges
    private AtomicInteger activeSessions;
    private AtomicInteger activeUsers;

    // Timers
    private Timer authenticationTimer;
    private Timer tokenValidationTimer;
    private Timer twoFactorVerificationTimer;


    public SecurityMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.counters = new ConcurrentHashMap<>();
        this.timers = new ConcurrentHashMap<>();
        initializeMetrics();
    }

    private void initializeMetrics() {
        // Initialize counters
        loginAttemptsCounter = Counter
                .builder("security.authentication.attempts")
                .description("Total number of login attempts")
                .register(meterRegistry);
        loginSuccessCounter = Counter.builder("security.authentication.success")
                .description("Total number of successful logins")
                .register(meterRegistry);

        loginFailureCounter = Counter.builder("security.authentication.failures")
                .description("Total number of failed logins")
                .register(meterRegistry);

        twoFactorAttemptsCounter = Counter.builder("security.twofactor.attempts")
                .description("Total number of 2FA verification attempts")
                .register(meterRegistry);

        twoFactorSuccessCounter = Counter.builder("security.twofactor.success")
                .description("Total number of successful 2FA verifications")
                .register(meterRegistry);

        twoFactorFailureCounter = Counter.builder("security.twofactor.failures")
                .description("Total number of failed 2FA verifications")
                .register(meterRegistry);

        tokenRefreshCounter = Counter.builder("security.token.refresh")
                .description("Total number of token refresh attempts")
                .register(meterRegistry);

        tokenRefreshFailureCounter = Counter.builder("security.token.refresh.failures")
                .description("Total number of failed token refresh attempts")
                .register(meterRegistry);

        userRegistrationCounter = Counter.builder("security.user.registration")
                .description("Total number of user registrations")
                .register(meterRegistry);

        // Initialize gauges

        activeSessions = meterRegistry.gauge("security.sessions.active",new AtomicInteger(0));

        activeUsers = meterRegistry.gauge("security.users.active", new AtomicInteger(0));

        // Initialize timers
        authenticationTimer = Timer.builder("security.authentication.duration")
                .description("Time taken for authentication")
                .register(meterRegistry);

        tokenValidationTimer = Timer.builder("security.token.validation.duration")
                .description("Time taken for token validation")
                .register(meterRegistry);

        twoFactorVerificationTimer = Timer.builder("security.twofactor.verification.duration")
                .description("Time taken for 2FA verification")
                .register(meterRegistry);
    }

    // Authentication metrics
    public void recordLoginAttempt(boolean success){
        loginAttemptsCounter.increment();
        if(success){
            loginSuccessCounter.increment();
            activeUsers.incrementAndGet();
        }else {
            loginFailureCounter.increment();
        }
    }
    public void recordLogout(){
        activeUsers.decrementAndGet();
    }

    // 2FA metrics
    public void recordTwoFactorAttempt(boolean success) {
        twoFactorAttemptsCounter.increment();
        if (success) {
            twoFactorSuccessCounter.increment();
        } else {
            twoFactorFailureCounter.increment();
        }
    }

    // Token metrics
    public void recordTokenRefresh(boolean success) {
        tokenRefreshCounter.increment();
        if (!success) {
            tokenRefreshFailureCounter.increment();
        }
    }

    // User registration metrics
    public void recordUserRegistration() {
        userRegistrationCounter.increment();
    }

    // Timer methods
    public Timer.Sample startAuthenticationTimer(){
        return Timer.start(meterRegistry);
    }

    public void stopAuthenticationTimer(Timer.Sample sample){
        sample.stop(authenticationTimer);
    }

    public Timer.Sample startTokenValidationTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopTokenValidationTimer(Timer.Sample sample) {
        sample.stop(tokenValidationTimer);
    }

    public Timer.Sample startTwoFactorVerificationTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopTwoFactorVerificationTimer(Timer.Sample sample) {
        sample.stop(twoFactorVerificationTimer);
    }

    // Gauge updates
    public void setActiveSessions(int count) {
        activeSessions.set(count);
    }

    public void setActiveUsers(int count) {
        activeUsers.set(count);
    }

    // Custom counter creation
    public Counter createCounter(String name, String description, String... tags){
        String key = name+String.join("", tags);
        return  counters.computeIfAbsent(key, k-> Counter.builder(name)
                .description(description)
                .tags(tags)
                .register(meterRegistry));
    }

    // Custom timer creation
    public Timer createTimer(String name, String description, String... tags) {
        String key = name + String.join("", tags);
        return timers.computeIfAbsent(key, k ->
                Timer.builder(name)
                        .description(description)
                        .tags(tags)
                        .register(meterRegistry)
        );
    }

    /**
     * Get all metrics as a map for API responses
     */
    public SecurityMetricsSnapshot getMetricsSnapshot() {
        return SecurityMetricsSnapshot.builder()
                .loginAttempts(loginAttemptsCounter.count())
                .loginSuccess(loginSuccessCounter.count())
                .loginFailures(loginFailureCounter.count())
                .twoFactorAttempts(twoFactorAttemptsCounter.count())
                .twoFactorSuccess(twoFactorSuccessCounter.count())
                .twoFactorFailures(twoFactorFailureCounter.count())
                .tokenRefreshes(tokenRefreshCounter.count())
                .tokenRefreshFailures(tokenRefreshFailureCounter.count())
                .userRegistrations(userRegistrationCounter.count())
                .activeSessions(activeSessions.get())
                .activeUsers(activeUsers.get())
                .build();
    }


}
