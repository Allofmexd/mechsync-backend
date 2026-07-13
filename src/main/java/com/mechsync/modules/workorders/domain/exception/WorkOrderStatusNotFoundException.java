package com.mechsync.modules.workorders.domain.exception;
public class WorkOrderStatusNotFoundException extends RuntimeException {
    public WorkOrderStatusNotFoundException(Long id) { super("Work order status not found or invalid for WORK_ORDERS: " + id); }
}
