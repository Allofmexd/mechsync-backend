package com.mechsync.modules.workorders.web.controller;

import com.mechsync.modules.workorders.domain.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record WorkOrderRevisionResponse(
        Long id,
        Long workOrderId,
        Integer revisionNumber,
        String status,
        boolean isCurrent,
        boolean isFinalApproved,
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
        Long createdByUserId,
        Long approvedByUserId,
        LocalDateTime approvedAt,
        String acceptedByName,
        Long acceptedByUserId,
        LocalDateTime acceptedAt,
        String acceptanceMethod,
        String acceptanceNotes,
        long lockVersion,
        List<ServiceLineResponse> services,
        List<PartLineResponse> parts,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static WorkOrderRevisionResponse from(WorkOrderRevision revision) {
        return new WorkOrderRevisionResponse(
                revision.id(), revision.workOrderId(), revision.revisionNumber(), revision.status().name(),
                revision.current(), revision.finalApproved(), revision.technicianId(),
                revision.estimatedStartDate(), revision.estimatedDeliveryDate(), revision.estimatedHours(),
                revision.currency(), revision.applyIva(), revision.ivaRate(), revision.subtotalAmount(),
                revision.ivaAmount(), revision.totalAmount(), revision.taxTreatmentNotes(),
                revision.technicalObservations(), revision.customerNotes(), revision.changeReason(),
                revision.createdByUserId(), revision.approvedByUserId(), revision.approvedAt(),
                revision.acceptedByName(), revision.acceptedByUserId(), revision.acceptedAt(),
                revision.acceptanceMethod(), revision.acceptanceNotes(), revision.lockVersion(),
                revision.services().stream().map(ServiceLineResponse::from).toList(),
                revision.parts().stream().map(PartLineResponse::from).toList(),
                revision.createdAt(), revision.updatedAt());
    }

    public record ServiceLineResponse(
            Long id, Integer lineNumber, Long serviceId, String nameSnapshot,
            String descriptionSnapshot, BigDecimal quantity, BigDecimal unitPrice,
            BigDecimal lineSubtotal, String notes) {
        static ServiceLineResponse from(WorkOrderRevisionServiceLine line) {
            return new ServiceLineResponse(line.id(), line.lineNumber(), line.serviceId(),
                    line.nameSnapshot(), line.descriptionSnapshot(), line.quantity(), line.unitPrice(),
                    line.lineSubtotal(), line.notes());
        }
    }

    public record PartLineResponse(
            Long id, Integer lineNumber, Long partId, String nameSnapshot, String partNumberSnapshot,
            String descriptionSnapshot, BigDecimal quantity, BigDecimal unitPrice,
            BigDecimal lineSubtotal, String notes) {
        static PartLineResponse from(WorkOrderRevisionPartLine line) {
            return new PartLineResponse(line.id(), line.lineNumber(), line.partId(), line.nameSnapshot(),
                    line.partNumberSnapshot(), line.descriptionSnapshot(), line.quantity(), line.unitPrice(),
                    line.lineSubtotal(), line.notes());
        }
    }
}
