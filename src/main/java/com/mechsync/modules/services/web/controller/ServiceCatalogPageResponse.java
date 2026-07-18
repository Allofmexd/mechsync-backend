package com.mechsync.modules.services.web.controller;

import com.mechsync.modules.services.application.dto.ServiceCatalogPage;
import java.util.List;

public record ServiceCatalogPageResponse(
        List<ServiceCatalogResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public ServiceCatalogPageResponse {
        content = List.copyOf(content);
    }

    public static ServiceCatalogPageResponse from(ServiceCatalogPage result) {
        return new ServiceCatalogPageResponse(
                result.content().stream().map(ServiceCatalogResponse::from).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages());
    }
}
