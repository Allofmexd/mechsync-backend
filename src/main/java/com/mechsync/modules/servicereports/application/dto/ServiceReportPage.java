package com.mechsync.modules.servicereports.application.dto;

import com.mechsync.modules.servicereports.domain.model.ServiceReport;
import java.util.List;

public record ServiceReportPage(
        List<ServiceReport> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
