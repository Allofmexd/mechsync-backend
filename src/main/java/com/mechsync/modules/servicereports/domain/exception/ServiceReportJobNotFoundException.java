package com.mechsync.modules.servicereports.domain.exception;

public class ServiceReportJobNotFoundException extends RuntimeException {
    public ServiceReportJobNotFoundException(Long jobId) {
        super("Job not found for Service Report: " + jobId);
    }
}
