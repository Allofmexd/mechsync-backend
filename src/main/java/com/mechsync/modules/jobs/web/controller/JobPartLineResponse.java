package com.mechsync.modules.jobs.web.controller;

import com.mechsync.modules.jobs.domain.model.JobPartLine;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record JobPartLineResponse(
        Long id,
        Long jobId,
        Long partId,
        String partName,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineSubtotal,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static JobPartLineResponse from(JobPartLine line) {
        return new JobPartLineResponse(line.id(), line.jobId(), line.partId(), line.partName(),
                line.quantity(), line.unitPrice(), line.lineSubtotal(), line.createdAt(),
                line.updatedAt());
    }
}
