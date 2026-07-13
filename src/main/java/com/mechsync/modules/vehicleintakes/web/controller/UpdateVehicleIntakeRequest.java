package com.mechsync.modules.vehicleintakes.web.controller;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record UpdateVehicleIntakeRequest(
        @Positive Long technicianId,
        LocalDateTime intakeDate,
        @PositiveOrZero Integer intakeMileage,
        @NotBlank String reportedProblem,
        @Pattern(regexp = ".*\\S.*", message = "must not be blank") String initialObservations,
        @NotNull @Positive Long statusId) { }
