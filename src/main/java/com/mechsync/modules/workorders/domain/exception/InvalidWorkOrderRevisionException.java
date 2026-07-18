package com.mechsync.modules.workorders.domain.exception;

public class InvalidWorkOrderRevisionException extends RuntimeException {
    public InvalidWorkOrderRevisionException(String message) {
        super(message);
    }
}
