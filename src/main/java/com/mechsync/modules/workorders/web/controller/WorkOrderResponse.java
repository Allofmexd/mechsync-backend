package com.mechsync.modules.workorders.web.controller;
import com.mechsync.modules.workorders.domain.model.WorkOrder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record WorkOrderResponse(Long id,Long vehicleIntakeId,Long technicianId,LocalDateTime workOrderDate,
 LocalDateTime estimatedStartDate,LocalDateTime estimatedDeliveryDate,BigDecimal estimatedHours,
 BigDecimal estimatedSubtotal,BigDecimal estimatedIva,BigDecimal estimatedTotal,String technicalObservations,
 Long statusId,LocalDateTime createdAt,LocalDateTime updatedAt){
 public static WorkOrderResponse from(WorkOrder o){return new WorkOrderResponse(o.id(),o.vehicleIntakeId(),
  o.technicianId(),o.workOrderDate(),o.estimatedStartDate(),o.estimatedDeliveryDate(),o.estimatedHours(),
  o.estimatedSubtotal(),o.estimatedIva(),o.estimatedTotal(),o.technicalObservations(),o.statusId(),
  o.createdAt(),o.updatedAt());}}
