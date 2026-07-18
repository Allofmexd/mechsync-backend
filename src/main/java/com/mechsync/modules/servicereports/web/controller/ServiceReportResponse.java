package com.mechsync.modules.servicereports.web.controller;

import com.mechsync.modules.servicereports.domain.model.ServiceReport;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServiceReportResponse(
        Long id,
        Long jobId,
        String status,
        LocalDateTime reportDate,
        String finalDescription,
        BigDecimal finalSubtotal,
        BigDecimal finalIva,
        BigDecimal finalTotal,
        boolean customerConfirmation,
        LocalDateTime deliveredAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static ServiceReportResponse from(ServiceReport report) {
        return new ServiceReportResponse(report.id(), report.jobId(), report.status().name(),
                report.reportDate(), report.finalDescription(), report.finalSubtotal(),
                report.finalIva(), report.finalTotal(), report.customerConfirmation(),
                report.deliveredAt(), report.createdAt(), report.updatedAt());
    }
}
