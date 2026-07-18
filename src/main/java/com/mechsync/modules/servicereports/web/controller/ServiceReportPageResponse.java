package com.mechsync.modules.servicereports.web.controller;

import com.mechsync.modules.servicereports.application.dto.ServiceReportPage;
import java.util.List;

public record ServiceReportPageResponse(
        List<ServiceReportResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
    public static ServiceReportPageResponse from(ServiceReportPage page) {
        return new ServiceReportPageResponse(page.content().stream()
                .map(ServiceReportResponse::from).toList(), page.page(), page.size(),
                page.totalElements(), page.totalPages());
    }
}
