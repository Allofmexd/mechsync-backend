package com.mechsync.modules.technicians.web.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record CreateTechnicianRequest(
        @NotNull @Positive Long userId,
        @NotNull @Positive Long specialtyId,
        LocalDate hireDate) {
}
