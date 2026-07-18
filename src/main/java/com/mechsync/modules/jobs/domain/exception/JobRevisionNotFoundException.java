package com.mechsync.modules.jobs.domain.exception;

public class JobRevisionNotFoundException extends RuntimeException {
    public JobRevisionNotFoundException(Long id) { super("Work Order Revision not found: " + id); }
}
