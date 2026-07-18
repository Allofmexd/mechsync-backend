package com.mechsync.modules.workorders.domain.exception;

public class WorkOrderRevisionConflictException extends RuntimeException {
    public WorkOrderRevisionConflictException(String message) {
        super(message);
    }
}
