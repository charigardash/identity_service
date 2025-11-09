package com.learning.health;


import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    @Transactional(readOnly = true)
    public Health health() {
        try {
            // Check database connectivity
            String result = jdbcTemplate.queryForObject("SELECT 'OK' FROM dual", String.class);

            // Check user table accessibility
            Long userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);

            // Check refresh tokens table
            Long tokenCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM refresh_tokens", Long.class);

            return Health.up()
                    .withDetail("database", "Connected")
                    .withDetail("users_table", "Accessible")
                    .withDetail("tokens_table", "Accessible")
                    .withDetail("total_users", userCount)
                    .withDetail("total_tokens", tokenCount)
                    .build();

        } catch (DataAccessException e) {
            return Health.down()
                    .withDetail("database", "Connection failed")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
