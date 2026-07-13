package com.mechsync.modules.auth.infrastructure.security;

import com.mechsync.modules.auth.application.port.out.PasswordVerifierPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordVerifier implements PasswordVerifierPort {

    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordVerifier(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        return passwordEncoder.matches(rawPassword, passwordHash);
    }
}
