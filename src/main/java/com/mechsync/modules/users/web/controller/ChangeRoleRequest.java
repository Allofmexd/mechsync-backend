package com.mechsync.modules.users.web.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangeRoleRequest(
        @NotBlank @Pattern(regexp = "ADMINISTRADOR|TECNICO|CLIENTE") String role) {
}
