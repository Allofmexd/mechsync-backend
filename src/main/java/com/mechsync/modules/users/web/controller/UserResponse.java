package com.mechsync.modules.users.web.controller;

import com.mechsync.modules.users.domain.model.Role;
import com.mechsync.modules.users.domain.model.User;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String phone,
        String email,
        Set<String> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public UserResponse {
        roles = Collections.unmodifiableSet(new TreeSet<>(roles));
    }

    public static UserResponse from(User user) {
        return new UserResponse(
                user.id(),
                user.firstName(),
                user.lastName(),
                user.phone(),
                user.email(),
                user.roles().stream().map(Role::name).collect(java.util.stream.Collectors.toSet()),
                user.createdAt(),
                user.updatedAt());
    }
}
