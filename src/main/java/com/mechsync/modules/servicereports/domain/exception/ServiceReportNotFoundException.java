package com.mechsync.modules.servicereports.domain.exception;

public class ServiceReportNotFoundException extends RuntimeException {
    public ServiceReportNotFoundException(Long reportId) {
        super("Service Report not found: " + reportId);
    }

    public static ServiceReportNotFoundException forJob(Long jobId) {
        return new ServiceReportNotFoundException("Service Report not found for Job: " + jobId);
    }

    private ServiceReportNotFoundException(String message) {
        super(message);
    }
}
