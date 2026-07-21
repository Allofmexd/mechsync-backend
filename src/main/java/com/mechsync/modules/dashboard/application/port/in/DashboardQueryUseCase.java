package com.mechsync.modules.dashboard.application.port.in;

import com.mechsync.modules.dashboard.domain.model.*;
import java.util.List;

public interface DashboardQueryUseCase {
    DashboardSummary summary(DashboardPeriod period);
    List<StatusMetric> workOrdersByStatus(DashboardPeriod period);
    List<StatusMetric> jobsByStatus(DashboardPeriod period);
    List<MonthlyRevenueMetric> revenueByMonth(DashboardPeriod period);
    List<TopServiceMetric> topServices(DashboardPeriod period, int limit);
    List<TechnicianWorkloadMetric> technicianWorkload(DashboardPeriod period);
}
