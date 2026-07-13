package com.mechsync.modules.users.web.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank @Size(min = 8, max = 200) String newPassword) {

    @Override
    public String toString() {
        return "ChangePasswordRequest[newPassword=[REDACTED]]";
    }
}
