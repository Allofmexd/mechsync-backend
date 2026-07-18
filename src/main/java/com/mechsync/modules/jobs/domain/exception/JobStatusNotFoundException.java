package com.mechsync.modules.jobs.domain.exception;

public class JobStatusNotFoundException extends RuntimeException {
    public JobStatusNotFoundException(String code) { super("JOBS status not found: " + code); }
}
