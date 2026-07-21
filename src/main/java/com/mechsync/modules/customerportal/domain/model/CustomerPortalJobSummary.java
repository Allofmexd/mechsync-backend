package com.mechsync.modules.customerportal.domain.model;

import java.time.LocalDateTime;

public record CustomerPortalJobSummary(
        Long jobId,
        Long workOrderId,
        CustomerPortalVehicleInfo vehicle,
        String visibleStatus,
        LocalDateTime scheduledStartDate,
        LocalDateTime actualStartDate,
        LocalDateTime actualEndDate,
        String technicianName,
        String technicianSpecialty,
        boolean reportAvailable) {
}
