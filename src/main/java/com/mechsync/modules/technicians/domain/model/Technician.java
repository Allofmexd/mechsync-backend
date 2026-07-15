package com.mechsync.modules.technicians.domain.model;

import java.time.LocalDate;

public record Technician(
        Long id,
        Long userId,
        String firstName,
        String lastName,
        String email,
        String phone,
        Long specialtyId,
        String specialtyCode,
        LocalDate hireDate) {
}
