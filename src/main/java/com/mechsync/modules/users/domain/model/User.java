package com.mechsync.modules.users.domain.model;

import java.time.LocalDateTime;
import java.util.Set;

public record User(
        Long id,
        String firstName,
        String lastName,
        String phone,
        String email,
        String passwordHash,
        Set<Role> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public User {
        roles = Set.copyOf(roles);
    }
}
