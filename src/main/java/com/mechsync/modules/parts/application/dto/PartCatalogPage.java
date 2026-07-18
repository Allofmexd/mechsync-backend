package com.mechsync.modules.parts.application.dto;

import com.mechsync.modules.parts.domain.model.PartCatalogItem;
import java.util.List;

public record PartCatalogPage(
        List<PartCatalogItem> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public PartCatalogPage {
        content = List.copyOf(content);
    }
}
