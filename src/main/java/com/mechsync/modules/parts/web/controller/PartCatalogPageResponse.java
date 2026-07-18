package com.mechsync.modules.parts.web.controller;

import com.mechsync.modules.parts.application.dto.PartCatalogPage;
import java.util.List;

public record PartCatalogPageResponse(
        List<PartCatalogResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public PartCatalogPageResponse {
        content = List.copyOf(content);
    }

    public static PartCatalogPageResponse from(PartCatalogPage result) {
        return new PartCatalogPageResponse(
                result.content().stream().map(PartCatalogResponse::from).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages());
    }
}
