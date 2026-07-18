package com.mechsync.modules.technicians.application.dto;

import java.time.LocalDate;

public record CreateTechnicianCommand(
        Long userId,
        Long specialtyId,
        LocalDate hireDate) {
}
