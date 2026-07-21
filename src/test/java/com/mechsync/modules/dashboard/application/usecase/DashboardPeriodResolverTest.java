package com.mechsync.modules.dashboard.application.usecase;

import static org.junit.jupiter.api.Assertions.*;

import com.mechsync.modules.dashboard.domain.exception.InvalidDashboardPeriodException;
import com.mechsync.modules.dashboard.domain.model.DashboardPeriod;
import java.time.*;
import org.junit.jupiter.api.Test;

class DashboardPeriodResolverTest {
    private final DashboardPeriodResolver resolver = new DashboardPeriodResolver(
            Clock.fixed(Instant.parse("2026-07-21T12:00:00Z"), ZoneOffset.UTC));

    @Test
    void defaultsToCurrentMonthThroughToday() {
        DashboardPeriod period = resolver.resolve(null, null);
        assertEquals(LocalDate.of(2026, 7, 1), period.from());
        assertEquals(LocalDate.of(2026, 7, 21), period.to());
        assertEquals(LocalDateTime.of(2026, 7, 1, 0, 0), period.fromInclusive());
        assertEquals(LocalDateTime.of(2026, 7, 22, 0, 0), period.toExclusive());
    }

    @Test
    void acceptsValidInclusiveRange() {
        DashboardPeriod period = resolver.resolve(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 7, 21));
        assertEquals(LocalDate.of(2026, 1, 1), period.from());
        assertEquals(LocalDate.of(2026, 7, 21), period.to());
    }

    @Test
    void rejectsInvertedAndExcessiveRanges() {
        assertThrows(InvalidDashboardPeriodException.class, () -> resolver.resolve(
                LocalDate.of(2026, 7, 22), LocalDate.of(2026, 7, 21)));
        assertThrows(InvalidDashboardPeriodException.class, () -> resolver.resolve(
                LocalDate.of(2024, 1, 1), LocalDate.of(2026, 1, 1)));
    }
}
