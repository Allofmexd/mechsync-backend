package com.mechsync.modules.users.domain.exception;

public class UserCustomerRoleConflictException extends RuntimeException {

    public UserCustomerRoleConflictException(Long userId) {
        super("User " + userId + " has a customer profile and must keep CLIENTE role");
    }
}
