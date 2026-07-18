package com.mechsync.modules.services.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServiceCatalogItem(
        Long id,
        String name,
        String description,
        BigDecimal basePrice,
        BigDecimal estimatedHours,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
