package com.mechsync.modules.dashboard.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mechsync.modules.dashboard.domain.model.*;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

class DashboardJdbcAdapterTest {
    private JdbcTemplate jdbc;
    private DashboardJdbcAdapter adapter;
    private DashboardPeriod period;

    @BeforeEach
    void setUp() {
        jdbc = mock(JdbcTemplate.class);
        adapter = new DashboardJdbcAdapter(jdbc);
        period = new DashboardPeriod(LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void executesOneAggregateQueryPerMetricWithBoundPeriod() throws Exception {
        DashboardSummary summary = new DashboardSummary(1, 2, 3, 4, 5,
                new BigDecimal("10.00"), "MXN", period.from(), period.to());
        doReturn(List.of(summary), List.of(), List.of(), List.of(), List.of(), List.of())
                .when(jdbc).query(anyString(), any(PreparedStatementSetter.class),
                        any(RowMapper.class));

        assertSame(summary, adapter.loadSummary(period));
        adapter.loadWorkOrdersByStatus(period);
        adapter.loadJobsByStatus(period);
        adapter.loadRevenueByMonth(period);
        adapter.loadTopServices(period, 5);
        adapter.loadTechnicianWorkload(period);

        ArgumentCaptor<PreparedStatementSetter> parameters =
                ArgumentCaptor.forClass(PreparedStatementSetter.class);
        verify(jdbc).query(eq(DashboardJdbcAdapter.TOP_SERVICES_SQL), parameters.capture(),
                any(RowMapper.class));
        PreparedStatement statement = mock(PreparedStatement.class);
        parameters.getValue().setValues(statement);
        verify(statement).setObject(1, period.fromInclusive());
        verify(statement).setObject(2, period.toExclusive());
        verify(statement).setInt(3, 5);
    }

    @Test
    void sqlUsesAuthoritativeFieldsAndServerSideAggregates() {
        assertAll(
                () -> assertTrue(DashboardJdbcAdapter.SUMMARY_SQL.contains("SUM(report.final_total)")),
                () -> assertTrue(DashboardJdbcAdapter.SUMMARY_SQL.contains("report.delivered_at")),
                () -> assertTrue(DashboardJdbcAdapter.SUMMARY_SQL.contains("status.name = 'ENTREGADO'")),
                () -> assertTrue(DashboardJdbcAdapter.WORK_ORDERS_BY_STATUS_SQL.contains("GROUP BY")),
                () -> assertTrue(DashboardJdbcAdapter.JOBS_BY_STATUS_SQL.contains("GROUP BY")),
                () -> assertTrue(DashboardJdbcAdapter.REVENUE_BY_MONTH_SQL.contains("DATE_FORMAT")),
                () -> assertTrue(DashboardJdbcAdapter.TOP_SERVICES_SQL.contains("SUM(line.quantity)")),
                () -> assertTrue(DashboardJdbcAdapter.TOP_SERVICES_SQL.contains("LIMIT ?")),
                () -> assertTrue(DashboardJdbcAdapter.TECHNICIAN_WORKLOAD_SQL.contains("GROUP BY")));
    }
}
