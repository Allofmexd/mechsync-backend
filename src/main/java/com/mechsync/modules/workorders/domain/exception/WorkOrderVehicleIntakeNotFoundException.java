package com.mechsync.modules.workorders.domain.exception;
public class WorkOrderVehicleIntakeNotFoundException extends RuntimeException {
    public WorkOrderVehicleIntakeNotFoundException(Long id) { super("Vehicle intake not found for work order: " + id); }
}
