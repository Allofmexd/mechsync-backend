package com.mechsync.modules.customers.domain.exception;

public class CustomerUserRoleRequiredException extends RuntimeException {

    public CustomerUserRoleRequiredException(Long userId) {
        super("User " + userId + " must have CLIENTE role");
    }
}
