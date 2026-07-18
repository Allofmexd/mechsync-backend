package com.mechsync.modules.jobs.domain.exception;

public class JobConflictException extends RuntimeException {
    public JobConflictException(String message) { super(message); }
}
