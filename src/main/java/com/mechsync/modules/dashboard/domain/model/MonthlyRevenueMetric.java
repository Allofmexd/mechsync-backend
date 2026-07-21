package com.mechsync.modules.dashboard.domain.model;

import java.math.BigDecimal;

public record MonthlyRevenueMetric(
        String period,
        int year,
        int month,
        BigDecimal total) {
}
