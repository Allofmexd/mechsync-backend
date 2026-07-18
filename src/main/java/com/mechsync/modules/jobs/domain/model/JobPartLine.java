package com.mechsync.modules.jobs.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record JobPartLine(
        Long id,
        Long jobId,
        Long partId,
        String partName,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineSubtotal,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
