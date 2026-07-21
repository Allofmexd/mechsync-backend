package com.mechsync.modules.customerportal.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CustomerPortalWorkOrderSummary(
        Long workOrderId,
        Long intakeId,
        CustomerPortalVehicleInfo vehicle,
        LocalDateTime workOrderDate,
        String visibleStatus,
        LocalDateTime estimatedStartDate,
        LocalDateTime estimatedDeliveryDate,
        BigDecimal estimatedHours,
        boolean quotationAvailable,
        String quotationStatus,
        BigDecimal quotationTotal,
        String quotationCurrency,
        Long jobId) {
}
