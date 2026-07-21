package com.mechsync.modules.dashboard.application.usecase;

import com.mechsync.modules.dashboard.application.port.in.DashboardQueryUseCase;
import com.mechsync.modules.dashboard.application.port.out.DashboardMetricsPort;
import com.mechsync.modules.dashboard.domain.model.*;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService implements DashboardQueryUseCase {
    private final DashboardMetricsPort metrics;

    public DashboardService(DashboardMetricsPort metrics) {
        this.metrics = metrics;
    }

    @Override
    public DashboardSummary summary(DashboardPeriod period) {
        return metrics.loadSummary(period);
    }

    @Override
    public List<StatusMetric> workOrdersByStatus(DashboardPeriod period) {
        return metrics.loadWorkOrdersByStatus(period);
    }

    @Override
    public List<StatusMetric> jobsByStatus(DashboardPeriod period) {
        return metrics.loadJobsByStatus(period);
    }

    @Override
    public List<MonthlyRevenueMetric> revenueByMonth(DashboardPeriod period) {
        Map<YearMonth, BigDecimal> totals = new HashMap<>();
        metrics.loadRevenueByMonth(period).forEach(metric -> totals.put(
                YearMonth.of(metric.year(), metric.month()), metric.total()));

        List<MonthlyRevenueMetric> completed = new ArrayList<>();
        YearMonth current = YearMonth.from(period.from());
        YearMonth last = YearMonth.from(period.to());
        while (!current.isAfter(last)) {
            completed.add(new MonthlyRevenueMetric(current.toString(), current.getYear(),
                    current.getMonthValue(), totals.getOrDefault(current, BigDecimal.ZERO)));
            current = current.plusMonths(1);
        }
        return List.copyOf(completed);
    }

    @Override
    public List<TopServiceMetric> topServices(DashboardPeriod period, int limit) {
        return metrics.loadTopServices(period, limit);
    }

    @Override
    public List<TechnicianWorkloadMetric> technicianWorkload(DashboardPeriod period) {
        return metrics.loadTechnicianWorkload(period);
    }
}
