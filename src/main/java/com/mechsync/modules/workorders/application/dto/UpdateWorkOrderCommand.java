package com.mechsync.modules.workorders.application.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record UpdateWorkOrderCommand(Long workOrderId, Long technicianId, LocalDateTime workOrderDate,
        LocalDateTime estimatedStartDate, LocalDateTime estimatedDeliveryDate, BigDecimal estimatedHours,
        BigDecimal estimatedSubtotal, BigDecimal estimatedIva, BigDecimal estimatedTotal,
        String technicalObservations, Long statusId) { }
