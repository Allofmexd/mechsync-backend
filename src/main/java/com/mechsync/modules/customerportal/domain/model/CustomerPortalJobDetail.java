package com.mechsync.modules.customerportal.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public record CustomerPortalJobDetail(
        Long jobId,
        Long workOrderId,
        CustomerPortalVehicleInfo vehicle,
        String visibleStatus,
        LocalDateTime scheduledStartDate,
        LocalDateTime actualStartDate,
        LocalDateTime actualEndDate,
        String technicianName,
        String technicianSpecialty,
        Long quotationWorkOrderId,
        boolean reportAvailable,
        List<CustomerPortalJobService> services,
        List<CustomerPortalJobPart> parts) {

    public CustomerPortalJobDetail {
        services = services == null ? List.of() : List.copyOf(services);
        parts = parts == null ? List.of() : List.copyOf(parts);
    }
}
