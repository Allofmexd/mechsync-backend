package com.mechsync.modules.servicereports.domain.exception;

public class ServiceReportConflictException extends RuntimeException {
    public ServiceReportConflictException(String message) {
        super(message);
    }
}
