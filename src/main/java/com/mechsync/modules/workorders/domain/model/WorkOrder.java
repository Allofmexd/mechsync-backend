package com.mechsync.modules.workorders.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WorkOrder(Long id, Long vehicleIntakeId, Long technicianId, LocalDateTime workOrderDate,
        LocalDateTime estimatedStartDate, LocalDateTime estimatedDeliveryDate, BigDecimal estimatedHours,
        BigDecimal estimatedSubtotal, BigDecimal estimatedIva, BigDecimal estimatedTotal,
        String technicalObservations, Long statusId, LocalDateTime createdAt, LocalDateTime updatedAt) { }
