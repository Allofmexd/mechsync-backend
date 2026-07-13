package com.mechsync.modules.users.domain.exception;

public class SelfRoleChangeNotAllowedException extends RuntimeException {

    public SelfRoleChangeNotAllowedException() {
        super("Administrators cannot change their own role");
    }
}
