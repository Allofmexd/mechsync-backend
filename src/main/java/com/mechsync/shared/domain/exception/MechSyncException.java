package com.mechsync.shared.domain.exception;

public class MechSyncException extends RuntimeException {

    public MechSyncException(String message) {
        super(message);
    }

    public MechSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}
