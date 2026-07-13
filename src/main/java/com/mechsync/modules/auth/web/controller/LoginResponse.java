package com.mechsync.modules.auth.web.controller;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn,
        AuthUserResponse user) {
}
