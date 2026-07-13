package com.mechsync.modules.customers.domain.exception;

public class CustomerInUseException extends RuntimeException {

    public CustomerInUseException(Long customerId) {
        super("Customer cannot be deleted because it has associated vehicles: " + customerId);
    }
}
