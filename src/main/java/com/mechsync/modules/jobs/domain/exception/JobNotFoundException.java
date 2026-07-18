package com.mechsync.modules.jobs.domain.exception;

public class JobNotFoundException extends RuntimeException {
    public JobNotFoundException(Long id) { super("Job not found: " + id); }
}
