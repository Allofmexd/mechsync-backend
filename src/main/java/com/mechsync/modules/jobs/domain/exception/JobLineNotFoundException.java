package com.mechsync.modules.jobs.domain.exception;

public class JobLineNotFoundException extends RuntimeException {
    public JobLineNotFoundException(String lineType, Long lineId, Long jobId) {
        super(lineType + " line " + lineId + " was not found for Job " + jobId);
    }
}
