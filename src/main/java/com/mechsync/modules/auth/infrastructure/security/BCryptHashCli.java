package com.mechsync.modules.auth.infrastructure.security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class BCryptHashCli {

    private BCryptHashCli() {
    }

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String password = reader.readLine();
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        System.out.println(new BCryptPasswordEncoder(12).encode(password));
    }
}
