package com.mechsync.modules.customerportal.domain.model;

import java.time.LocalDateTime;

public record CustomerPortalHistoryEvent(
        String eventType,
        Long eventId,
        Long vehicleId,
        String vehicleLabel,
        LocalDateTime date,
        String title,
        String description,
        String status,
        Long relatedIntakeId,
        Long relatedWorkOrderId,
        Long relatedJobId) {
}
