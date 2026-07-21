package com.mechsync.modules.dashboard.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mechsync.modules.dashboard.application.port.out.DashboardMetricsPort;
import com.mechsync.modules.dashboard.domain.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {
    @Mock
    private DashboardMetricsPort metrics;

    private DashboardService service;
    private DashboardPeriod period;

    @BeforeEach
    void setUp() {
        service = new DashboardService(metrics);
        period = new DashboardPeriod(LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 9, 30));
    }

    @Test
    void delegatesSummaryAndAggregateCollections() {
        DashboardSummary summary = new DashboardSummary(3, 4, 1, 2, 1,
                new BigDecimal("116.00"), "MXN", period.from(), period.to());
        List<StatusMetric> statuses = List.of(new StatusMetric("PENDIENTE", "Pendiente", 2));
        List<TopServiceMetric> services = List.of(
                new TopServiceMetric(1L, "Diagnóstico", new BigDecimal("3.00")));
        List<TechnicianWorkloadMetric> workload = List.of(
                new TechnicianWorkloadMetric(1L, "Técnico Uno", 3, 1, 2));
        when(metrics.loadSummary(period)).thenReturn(summary);
        when(metrics.loadWorkOrdersByStatus(period)).thenReturn(statuses);
        when(metrics.loadJobsByStatus(period)).thenReturn(statuses);
        when(metrics.loadTopServices(period, 5)).thenReturn(services);
        when(metrics.loadTechnicianWorkload(period)).thenReturn(workload);

        assertSame(summary, service.summary(period));
        assertSame(statuses, service.workOrdersByStatus(period));
        assertSame(statuses, service.jobsByStatus(period));
        assertSame(services, service.topServices(period, 5));
        assertSame(workload, service.technicianWorkload(period));
    }

    @Test
    void completesMissingRevenueMonthsWithZeroInChronologicalOrder() {
        when(metrics.loadRevenueByMonth(period)).thenReturn(List.of(
                new MonthlyRevenueMetric("2026-08", 2026, 8, new BigDecimal("250.00"))));

        List<MonthlyRevenueMetric> result = service.revenueByMonth(period);

        assertEquals(List.of("2026-07", "2026-08", "2026-09"),
                result.stream().map(MonthlyRevenueMetric::period).toList());
        assertEquals(0, result.get(0).total().compareTo(BigDecimal.ZERO));
        assertEquals(0, result.get(1).total().compareTo(new BigDecimal("250.00")));
        assertEquals(0, result.get(2).total().compareTo(BigDecimal.ZERO));
    }

    @Test
    void preservesEmptyAggregateResponses() {
        when(metrics.loadRevenueByMonth(period)).thenReturn(List.of());
        when(metrics.loadTopServices(period, 5)).thenReturn(List.of());
        assertEquals(3, service.revenueByMonth(period).size());
        assertTrue(service.topServices(period, 5).isEmpty());
    }
}
