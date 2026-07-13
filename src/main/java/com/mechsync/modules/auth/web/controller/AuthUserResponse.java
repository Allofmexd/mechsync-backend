package com.mechsync.modules.auth.web.controller;

import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import java.util.Set;

public record AuthUserResponse(Long id, String email, Set<String> roles) {

    public static AuthUserResponse from(AuthenticatedUser user) {
        return new AuthUserResponse(user.id(), user.email(), user.roles());
    }
}
