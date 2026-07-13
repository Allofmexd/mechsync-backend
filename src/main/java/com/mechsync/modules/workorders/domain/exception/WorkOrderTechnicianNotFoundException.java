package com.mechsync.modules.workorders.domain.exception;
public class WorkOrderTechnicianNotFoundException extends RuntimeException {
    public WorkOrderTechnicianNotFoundException(Long id) { super("Technician not found for work order: " + id); }
}
