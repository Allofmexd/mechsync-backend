package com.mechsync.modules.jobs.web.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public record CreateJobRequest(
        @NotNull @Positive Long workOrderId,
        @NotNull @Positive Long initialApprovedRevisionId,
        @NotNull @Positive Long technicianId,
        LocalDateTime scheduledStartDate,
        String notes) {
}
