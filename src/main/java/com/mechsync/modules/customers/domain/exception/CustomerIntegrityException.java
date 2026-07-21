package com.mechsync.modules.customers.domain.exception;

public class CustomerIntegrityException extends RuntimeException {

    public CustomerIntegrityException() {
        super("No fue posible resolver el perfil de cliente.");
    }

    public CustomerIntegrityException(Throwable cause) {
        super("No fue posible resolver el perfil de cliente.", cause);
    }
}
