package com.mechsync.modules.users.web.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Size(max = 20) @Pattern(regexp = ".*\\S.*", message = "must not be blank") String phone,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 200) String password,
        @NotBlank @Pattern(regexp = "ADMINISTRADOR|TECNICO|CLIENTE") String role) {

    @Override
    public String toString() {
        return "CreateUserRequest[firstName=" + firstName + ", lastName=" + lastName
                + ", phone=" + phone + ", email=" + email
                + ", password=[REDACTED], role=" + role + "]";
    }
}
