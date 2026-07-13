package com.mechsync.modules.vehicleintakes.web.controller;

import com.mechsync.modules.vehicleintakes.domain.model.VehicleIntake;
import java.time.LocalDateTime;

public record VehicleIntakeResponse(Long id, Long vehicleId, Long technicianId, LocalDateTime intakeDate,
        Integer intakeMileage, String reportedProblem, String initialObservations, Long statusId,
        LocalDateTime createdAt, LocalDateTime updatedAt) {
    public static VehicleIntakeResponse from(VehicleIntake v) {
        return new VehicleIntakeResponse(v.id(), v.vehicleId(), v.technicianId(), v.intakeDate(),
                v.intakeMileage(), v.reportedProblem(), v.initialObservations(), v.statusId(),
                v.createdAt(), v.updatedAt());
    }
}
