package com.mechsync.modules.dashboard.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardSummary(
        long registeredCustomers,
        long registeredVehicles,
        long openVehicleIntakes,
        long activeWorkOrders,
        long jobsInProgress,
        BigDecimal periodRevenue,
        String currency,
        LocalDate from,
        LocalDate to) {
}
