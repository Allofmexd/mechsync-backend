package com.mechsync.modules.users.application.port.out;

public interface PasswordHasherPort {
    String hash(String rawPassword);
}
