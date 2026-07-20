package com.mechsync.modules.servicereports.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ServiceReportPdfData(
        Long reportId,
        Long jobId,
        ServiceReportStatus reportStatus,
        LocalDateTime reportDate,
        String finalDescription,
        BigDecimal finalSubtotal,
        BigDecimal finalIva,
        BigDecimal finalTotal,
        boolean customerConfirmation,
        LocalDateTime deliveredAt,
        Long workOrderId,
        Long vehicleIntakeId,
        Long technicianId,
        String technicianName,
        Long customerId,
        String customerName,
        Long vehicleId,
        String vehicleBrand,
        String vehicleModel,
        Integer vehicleYear,
        String licensePlate,
        String vin,
        Integer mileage,
        List<ServiceLine> services,
        List<PartLine> parts) {

    public ServiceReportPdfData {
        services = services == null ? List.of() : List.copyOf(services);
        parts = parts == null ? List.of() : List.copyOf(parts);
    }

    public record ServiceLine(
            String name,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal) {
    }

    public record PartLine(
            String name,
            BigDecimal quantity,
            String measurementUnit,
            BigDecimal unitPrice,
            BigDecimal subtotal) {
    }
}
