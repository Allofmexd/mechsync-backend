package com.mechsync.modules.vehicleintakes.application.dto;

import java.time.LocalDateTime;

public record CreateVehicleIntakeCommand(
        Long vehicleId, Long technicianId, LocalDateTime intakeDate, Integer intakeMileage,
        String reportedProblem, String initialObservations, Long statusId) {
}
