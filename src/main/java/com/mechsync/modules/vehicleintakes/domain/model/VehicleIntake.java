package com.mechsync.modules.vehicleintakes.domain.model;

import java.time.LocalDateTime;

public record VehicleIntake(
        Long id,
        Long vehicleId,
        Long technicianId,
        LocalDateTime intakeDate,
        Integer intakeMileage,
        String reportedProblem,
        String initialObservations,
        Long statusId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
