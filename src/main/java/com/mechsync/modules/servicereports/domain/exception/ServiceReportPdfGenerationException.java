package com.mechsync.modules.servicereports.domain.exception;

public class ServiceReportPdfGenerationException extends RuntimeException {
    public ServiceReportPdfGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceReportPdfGenerationException(String message) {
        super(message);
    }
}
