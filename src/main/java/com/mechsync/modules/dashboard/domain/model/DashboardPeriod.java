package com.mechsync.modules.dashboard.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DashboardPeriod(LocalDate from, LocalDate to) {
    public LocalDateTime fromInclusive() {
        return from.atStartOfDay();
    }

    public LocalDateTime toExclusive() {
        return to.plusDays(1).atStartOfDay();
    }
}
