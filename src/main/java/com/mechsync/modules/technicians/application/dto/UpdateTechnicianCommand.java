package com.mechsync.modules.technicians.application.dto;

import java.time.LocalDate;

public record UpdateTechnicianCommand(
        Long technicianId,
        Long specialtyId,
        LocalDate hireDate) {
}
