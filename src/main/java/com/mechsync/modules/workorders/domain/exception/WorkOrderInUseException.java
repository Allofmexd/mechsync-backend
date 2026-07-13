package com.mechsync.modules.workorders.domain.exception;
public class WorkOrderInUseException extends RuntimeException {
    public WorkOrderInUseException(Long id) { super("Work order has dependent jobs or planned details: " + id); }
}
