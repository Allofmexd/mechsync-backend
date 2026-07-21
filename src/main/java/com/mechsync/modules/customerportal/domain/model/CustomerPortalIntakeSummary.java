package com.mechsync.modules.customerportal.domain.model;

import java.time.LocalDateTime;

public record CustomerPortalIntakeSummary(
        Long intakeId,
        Long vehicleId,
        String vehicleLabel,
        LocalDateTime intakeDate,
        Integer intakeMileage,
        String reportedProblem,
        String visibleStatus,
        LocalDateTime updatedAt) {
}
