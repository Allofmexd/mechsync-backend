package com.mechsync.modules.technicians.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Technician(
        Long id,
        Long userId,
        String firstName,
        String lastName,
        String email,
        String phone,
        Long specialtyId,
        String specialtyCode,
        LocalDate hireDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
