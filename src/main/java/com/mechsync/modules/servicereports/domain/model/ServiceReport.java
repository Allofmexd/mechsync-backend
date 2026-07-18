package com.mechsync.modules.servicereports.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServiceReport(
        Long id,
        Long jobId,
        ServiceReportStatus status,
        LocalDateTime reportDate,
        String finalDescription,
        BigDecimal finalSubtotal,
        BigDecimal finalIva,
        BigDecimal finalTotal,
        boolean customerConfirmation,
        LocalDateTime deliveredAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
