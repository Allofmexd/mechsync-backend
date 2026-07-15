package com.mechsync.modules.catalogs.domain.exception;

public class InvalidStatusContextException extends RuntimeException {

    public InvalidStatusContextException(String context) {
        super("Invalid status context: " + (context == null ? "null" : context));
    }
}
