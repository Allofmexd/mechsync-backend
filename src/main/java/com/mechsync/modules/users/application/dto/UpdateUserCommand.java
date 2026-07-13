package com.mechsync.modules.users.application.dto;

public record UpdateUserCommand(
        Long userId,
        String firstName,
        String lastName,
        String phone,
        String email) {
}
