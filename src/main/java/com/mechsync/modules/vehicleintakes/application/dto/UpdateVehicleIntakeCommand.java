package com.mechsync.modules.vehicleintakes.application.dto;

import java.time.LocalDateTime;

public record UpdateVehicleIntakeCommand(
        Long intakeId, Long technicianId, LocalDateTime intakeDate, Integer intakeMileage,
        String reportedProblem, String initialObservations, Long statusId) {
}
