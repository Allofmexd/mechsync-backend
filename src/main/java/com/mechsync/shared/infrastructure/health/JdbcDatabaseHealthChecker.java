package com.mechsync.shared.infrastructure.health;

import com.mechsync.shared.application.health.DatabaseHealthChecker;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JdbcDatabaseHealthChecker implements DatabaseHealthChecker {

    private static final String HEALTH_QUERY = "SELECT 1";

    private final JdbcTemplate jdbcTemplate;

    public JdbcDatabaseHealthChecker(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean isAvailable() {
        try {
            Integer result = jdbcTemplate.queryForObject(HEALTH_QUERY, Integer.class);
            return Integer.valueOf(1).equals(result);
        } catch (DataAccessException exception) {
            return false;
        }
    }
}
