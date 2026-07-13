package com.mechsync.modules.customers.domain.exception;

public class CustomerUserNotFoundException extends RuntimeException {

    public CustomerUserNotFoundException(Long userId) {
        super("User not found: " + userId);
    }
}
