package com.mechsync.modules.customers.domain.exception;

public class CustomerPortalAccessDeniedException extends RuntimeException {

    public CustomerPortalAccessDeniedException() {
        super("No tienes permiso para acceder al portal de cliente.");
    }
}
