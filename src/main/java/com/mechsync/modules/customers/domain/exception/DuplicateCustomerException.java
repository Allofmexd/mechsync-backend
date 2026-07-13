package com.mechsync.modules.customers.domain.exception;

public class DuplicateCustomerException extends RuntimeException {

    public DuplicateCustomerException(Long userId) {
        super("A customer already exists for user: " + userId);
    }
}
