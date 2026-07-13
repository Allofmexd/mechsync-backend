package com.mechsync.modules.auth.application.dto;

import com.mechsync.modules.auth.domain.model.AuthenticatedUser;

public record LoginResult(GeneratedToken token, AuthenticatedUser user) {
}
