package com.mechsync.modules.jobs.web.controller;

import com.mechsync.modules.jobs.domain.model.Job;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record JobResponse(
        Long id,
        Long workOrderId,
        Long initialApprovedRevisionId,
        Long technicianId,
        String status,
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

    public static JobResponse from(Job job) {
        return new JobResponse(job.id(), job.workOrderId(), job.initialApprovedRevisionId(),
                job.technicianId(), job.status().name(), job.scheduledStartDate(), job.startDate(),
                job.completionDate(), job.cancelledAt(), job.actualHours(),
                job.realSubtotalAmount(), job.realIvaAmount(), job.realTotalAmount(), job.notes(),
                job.cancellationNotes(), job.createdAt(), job.updatedAt());
    }
}
