package com.mechsync.modules.dashboard.application.usecase;

import com.mechsync.modules.dashboard.domain.exception.InvalidDashboardPeriodException;
import com.mechsync.modules.dashboard.domain.model.DashboardPeriod;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

@Component
public class DashboardPeriodResolver {
    static final long MAX_INCLUSIVE_DAYS = 731;

    private final Clock clock;

    public DashboardPeriodResolver() {
        this(Clock.systemDefaultZone());
    }

    DashboardPeriodResolver(Clock clock) {
        this.clock = clock;
    }

    public DashboardPeriod resolve(LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now(clock);
        LocalDate resolvedTo = to == null ? today : to;
        LocalDate resolvedFrom = from == null ? resolvedTo.withDayOfMonth(1) : from;

        if (resolvedFrom.isAfter(resolvedTo)) {
            throw new InvalidDashboardPeriodException("from must be before or equal to to");
        }
        long inclusiveDays = ChronoUnit.DAYS.between(resolvedFrom, resolvedTo) + 1;
        if (inclusiveDays > MAX_INCLUSIVE_DAYS) {
            throw new InvalidDashboardPeriodException(
                    "Dashboard period cannot exceed " + MAX_INCLUSIVE_DAYS + " days");
        }
        return new DashboardPeriod(resolvedFrom, resolvedTo);
    }
}
