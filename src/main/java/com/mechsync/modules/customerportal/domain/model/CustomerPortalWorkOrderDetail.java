package com.mechsync.modules.customerportal.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CustomerPortalWorkOrderDetail(
        Long workOrderId,
        Long intakeId,
        CustomerPortalVehicleInfo vehicle,
        LocalDateTime intakeDate,
        String reportedProblem,
        LocalDateTime workOrderDate,
        String visibleStatus,
        LocalDateTime estimatedStartDate,
        LocalDateTime estimatedDeliveryDate,
        BigDecimal estimatedHours,
        boolean quotationAvailable,
        Long jobId) {
}
