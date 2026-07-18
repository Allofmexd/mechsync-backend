package com.mechsync.modules.parts.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PartCatalogItem(
        Long id,
        String name,
        String description,
        BigDecimal unitPrice,
        Long measurementUnitId,
        String measurementUnitName,
        String measurementUnitAbbreviation,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
