package com.mechsync.modules.jobs.application.dto;

import java.time.LocalDateTime;

public record CreateJobCommand(
        Long workOrderId,
        Long initialApprovedRevisionId,
        Long technicianId,
        LocalDateTime scheduledStartDate,
        String notes) {
}
