package com.mechsync.modules.auth.domain.model;

public record UserCredentials(AuthenticatedUser user, String passwordHash) {
}
