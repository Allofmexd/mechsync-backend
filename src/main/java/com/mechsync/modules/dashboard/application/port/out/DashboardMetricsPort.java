package com.mechsync.modules.dashboard.application.port.out;

import com.mechsync.modules.dashboard.domain.model.*;
import java.util.List;

public interface DashboardMetricsPort {
    DashboardSummary loadSummary(DashboardPeriod period);
    List<StatusMetric> loadWorkOrdersByStatus(DashboardPeriod period);
    List<StatusMetric> loadJobsByStatus(DashboardPeriod period);
    List<MonthlyRevenueMetric> loadRevenueByMonth(DashboardPeriod period);
    List<TopServiceMetric> loadTopServices(DashboardPeriod period, int limit);
    List<TechnicianWorkloadMetric> loadTechnicianWorkload(DashboardPeriod period);
}
