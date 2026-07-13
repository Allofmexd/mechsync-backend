package com.mechsync.modules.auth.application.port.out;

public interface PasswordVerifierPort {

    boolean matches(String rawPassword, String passwordHash);
}
