package com.mechsync.modules.users.domain.exception;

public class DuplicateUserEmailException extends RuntimeException {

    public DuplicateUserEmailException(String email) {
        super("A user already exists with email: " + email);
    }
}
