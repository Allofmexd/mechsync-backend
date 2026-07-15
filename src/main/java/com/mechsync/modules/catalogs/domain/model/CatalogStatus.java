package com.mechsync.modules.catalogs.domain.model;

public record CatalogStatus(
        Long id,
        StatusContext context,
        String code,
        String description) {
}
