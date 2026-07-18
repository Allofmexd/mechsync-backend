package com.mechsync.modules.workorders.domain.exception;

public class WorkOrderRevisionNotFoundException extends RuntimeException {
    public WorkOrderRevisionNotFoundException(Long workOrderId, Long revisionId) {
        super(revisionId == null
                ? "Work Order revision not found for Work Order: " + workOrderId
                : "Work Order revision not found: " + revisionId + " for Work Order: " + workOrderId);
    }
}
