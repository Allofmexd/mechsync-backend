package com.mechsync.modules.users.application.dto;

public record CreateUserCommand(
        String firstName,
        String lastName,
        String phone,
        String email,
        String password,
        String role) {
}
