package com.mechsync.modules.specialties.web.controller;

import com.mechsync.modules.specialties.domain.model.Specialty;
import java.util.Locale;

public record SpecialtyResponse(
        Long id,
        String code,
        String name,
        String description) {

    static SpecialtyResponse from(Specialty specialty) {
        return new SpecialtyResponse(
                specialty.id(),
                specialty.name(),
                readableName(specialty.name()),
                specialty.description());
    }

    private static String readableName(String code) {
        String normalized = code.toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }
}
