package com.mechsync.modules.dashboard.web.controller;

import com.mechsync.modules.dashboard.domain.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class DashboardResponses {
    private DashboardResponses() {
    }

    public record Summary(
            long registeredCustomers,
            long registeredVehicles,
            long openVehicleIntakes,
            long activeWorkOrders,
            long jobsInProgress,
            BigDecimal periodRevenue,
            String currency,
            LocalDate from,
            LocalDate to) {
        static Summary from(DashboardSummary value) {
            return new Summary(value.registeredCustomers(), value.registeredVehicles(),
                    value.openVehicleIntakes(), value.activeWorkOrders(),
                    value.jobsInProgress(), value.periodRevenue(), value.currency(),
                    value.from(), value.to());
        }
    }

    public record Status(String statusCode, String statusName, long count) {
        static Status from(StatusMetric value) {
            return new Status(value.statusCode(), value.statusName(), value.count());
        }
    }

    public record Revenue(
            String period,
            int year,
            int month,
            BigDecimal total) {
        static Revenue from(MonthlyRevenueMetric value) {
            return new Revenue(value.period(), value.year(), value.month(), value.total());
        }
    }

    public record TopService(Long serviceId, String serviceName, BigDecimal quantity) {
        static TopService from(TopServiceMetric value) {
            return new TopService(value.serviceId(), value.serviceName(), value.quantity());
        }
    }

    public record TechnicianWorkload(
            Long technicianId,
            String technicianName,
            long totalJobs,
            long inProgressJobs,
            long completedJobs) {
        static TechnicianWorkload from(TechnicianWorkloadMetric value) {
            return new TechnicianWorkload(value.technicianId(), value.technicianName(),
                    value.totalJobs(), value.inProgressJobs(), value.completedJobs());
        }
    }
}
