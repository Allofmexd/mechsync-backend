package com.mechsync.modules.workorders.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CreateWorkOrderRevisionCommand(
        Long workOrderId,
        RevisionActor actor,
        Long technicianId,
        LocalDateTime estimatedStartDate,
        LocalDateTime estimatedDeliveryDate,
        BigDecimal estimatedHours,
        String currency,
        boolean applyIva,
        BigDecimal ivaRate,
        BigDecimal subtotalAmount,
        BigDecimal ivaAmount,
        BigDecimal totalAmount,
        String taxTreatmentNotes,
        String technicalObservations,
        String customerNotes,
        String changeReason,
        List<CreateRevisionServiceLineCommand> services,
        List<CreateRevisionPartLineCommand> parts) {
}
