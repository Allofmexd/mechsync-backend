package com.mechsync.modules.jobs.web.controller;

import com.mechsync.modules.jobs.domain.model.JobServiceLine;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record JobServiceLineResponse(
        Long id,
        Long jobId,
        Long serviceId,
        String serviceName,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineSubtotal,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static JobServiceLineResponse from(JobServiceLine line) {
        return new JobServiceLineResponse(line.id(), line.jobId(), line.serviceId(),
                line.serviceName(), line.quantity(), line.unitPrice(), line.lineSubtotal(),
                line.createdAt(), line.updatedAt());
    }
}
