package com.mechsync.modules.workorders.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record WorkOrderRevision(
        Long id,
        Long workOrderId,
        Integer revisionNumber,
        WorkOrderRevisionStatus status,
        Long technicianId,
        LocalDateTime estimatedStartDate,
        LocalDateTime estimatedDeliveryDate,
        BigDecimal estimatedHours,
        BigDecimal subtotalAmount,
        boolean applyIva,
        BigDecimal ivaRate,
        BigDecimal ivaAmount,
        BigDecimal totalAmount,
        String currency,
        String taxTreatmentNotes,
        String technicalObservations,
        String customerNotes,
        String changeReason,
        Long createdByUserId,
        Long approvedByUserId,
        LocalDateTime approvedAt,
        String acceptedByName,
        Long acceptedByUserId,
        LocalDateTime acceptedAt,
        String acceptanceMethod,
        String acceptanceNotes,
        long lockVersion,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean current,
        boolean finalApproved,
        List<WorkOrderRevisionServiceLine> services,
        List<WorkOrderRevisionPartLine> parts) {

    public WorkOrderRevision {
        services = services == null ? List.of() : List.copyOf(services);
        parts = parts == null ? List.of() : List.copyOf(parts);
    }
}
