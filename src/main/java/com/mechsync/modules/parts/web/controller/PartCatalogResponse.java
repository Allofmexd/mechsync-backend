package com.mechsync.modules.parts.web.controller;

import com.mechsync.modules.parts.domain.model.PartCatalogItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PartCatalogResponse(
        Long id,
        String name,
        String description,
        BigDecimal unitPrice,
        Long measurementUnitId,
        String measurementUnitName,
        String measurementUnitAbbreviation,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static PartCatalogResponse from(PartCatalogItem item) {
        return new PartCatalogResponse(item.id(), item.name(), item.description(), item.unitPrice(),
                item.measurementUnitId(), item.measurementUnitName(),
                item.measurementUnitAbbreviation(), item.createdAt(), item.updatedAt());
    }
}
