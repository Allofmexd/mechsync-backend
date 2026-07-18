package com.mechsync.modules.jobs.domain.exception;

public class JobWorkOrderNotFoundException extends RuntimeException {
    public JobWorkOrderNotFoundException(Long id) { super("Work Order not found: " + id); }
}
