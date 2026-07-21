package com.mechsync.modules.customerportal.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public record CustomerPortalIntakeDetail(
        Long intakeId,
        CustomerPortalVehicleInfo vehicle,
        LocalDateTime intakeDate,
        Integer intakeMileage,
        String reportedProblem,
        String visibleStatus,
        LocalDateTime updatedAt,
        List<CustomerPortalWorkOrderLink> workOrders) {

    public CustomerPortalIntakeDetail {
        workOrders = workOrders == null ? List.of() : List.copyOf(workOrders);
    }
}
