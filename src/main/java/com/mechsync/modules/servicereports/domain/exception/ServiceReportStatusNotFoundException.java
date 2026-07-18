package com.mechsync.modules.servicereports.domain.exception;

public class ServiceReportStatusNotFoundException extends RuntimeException {
    public ServiceReportStatusNotFoundException(String status) {
        super("Service Report status not found: " + status);
    }
}
