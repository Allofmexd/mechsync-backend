package com.mechsync.modules.services.application.dto;

import com.mechsync.modules.services.domain.model.ServiceCatalogItem;
import java.util.List;

public record ServiceCatalogPage(
        List<ServiceCatalogItem> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public ServiceCatalogPage {
        content = List.copyOf(content);
    }
}
