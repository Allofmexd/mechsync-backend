package com.mechsync.modules.workorders.domain.exception;
public class WorkOrderNotFoundException extends RuntimeException {
    public WorkOrderNotFoundException(Long id) { super("Work order not found: " + id); }
}
