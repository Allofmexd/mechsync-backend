package com.mechsync.modules.technicians.web.controller;

import com.mechsync.modules.technicians.domain.model.Technician;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

public record TechnicianResponse(
        Long id,
        Long userId,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phone,
        Long specialtyId,
        String specialtyCode,
        String specialtyName,
        LocalDate hireDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static TechnicianResponse from(Technician technician) {
        return new TechnicianResponse(
                technician.id(),
                technician.userId(),
                technician.firstName(),
                technician.lastName(),
                technician.firstName() + " " + technician.lastName(),
                technician.email(),
                technician.phone(),
                technician.specialtyId(),
                technician.specialtyCode(),
                readableName(technician.specialtyCode()),
                technician.hireDate(),
                technician.createdAt(),
                technician.updatedAt());
    }

    private static String readableName(String code) {
        String normalized = code.toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }
}
