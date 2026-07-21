package com.mechsync.modules.customers.domain.exception;

public class CustomerProfileRequiredException extends RuntimeException {

    public CustomerProfileRequiredException() {
        super("Tu cuenta no tiene un perfil de cliente asociado. Contacta al taller.");
    }
}
