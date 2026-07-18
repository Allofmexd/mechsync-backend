package com.mechsync.modules.services.web.controller;

import com.mechsync.modules.services.domain.model.ServiceCatalogItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServiceCatalogResponse(
        Long id,
        String name,
        String description,
        BigDecimal basePrice,
        BigDecimal estimatedHours,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static ServiceCatalogResponse from(ServiceCatalogItem item) {
        return new ServiceCatalogResponse(item.id(), item.name(), item.description(),
                item.basePrice(), item.estimatedHours(), item.createdAt(), item.updatedAt());
    }
}
