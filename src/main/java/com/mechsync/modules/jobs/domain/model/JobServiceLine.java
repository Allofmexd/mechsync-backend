package com.mechsync.modules.jobs.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record JobServiceLine(
        Long id,
        Long jobId,
        Long serviceId,
        String serviceName,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineSubtotal,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
