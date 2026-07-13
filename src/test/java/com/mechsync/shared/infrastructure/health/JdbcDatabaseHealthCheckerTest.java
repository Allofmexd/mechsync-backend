package com.mechsync.shared.infrastructure.health;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class JdbcDatabaseHealthCheckerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private JdbcDatabaseHealthChecker databaseHealthChecker;

    @BeforeEach
    void setUp() {
        databaseHealthChecker = new JdbcDatabaseHealthChecker(jdbcTemplate);
    }

    @Test
    void returnsAvailableWhenSelectOneSucceeds() {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);

        assertTrue(databaseHealthChecker.isAvailable());
    }

    @Test
    void returnsUnavailableWhenDatabaseAccessFails() {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
                .thenThrow(new DataAccessResourceFailureException("Database unavailable"));

        assertFalse(databaseHealthChecker.isAvailable());
    }
}
