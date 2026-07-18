package com.mechsync.modules.jobs.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Job(
        Long id,
        Long workOrderId,
        Long initialApprovedRevisionId,
        Long technicianId,
        JobStatus status,
        LocalDateTime scheduledStartDate,
        LocalDateTime startDate,
        LocalDateTime completionDate,
        LocalDateTime cancelledAt,
        BigDecimal actualHours,
        BigDecimal realSubtotalAmount,
        BigDecimal realIvaAmount,
        BigDecimal realTotalAmount,
        String notes,
        String cancellationNotes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
