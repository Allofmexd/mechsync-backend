package com.mechsync.modules.catalogs.web.controller;

import com.mechsync.modules.catalogs.domain.model.CatalogStatus;
import java.util.Locale;

public record CatalogStatusResponse(
        Long id,
        String code,
        String name,
        String context,
        String description) {

    public static CatalogStatusResponse from(CatalogStatus status) {
        return new CatalogStatusResponse(
                status.id(),
                status.code(),
                readableName(status.code()),
                status.context().name(),
                status.description());
    }

    private static String readableName(String code) {
        String normalized = code.toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }
}
