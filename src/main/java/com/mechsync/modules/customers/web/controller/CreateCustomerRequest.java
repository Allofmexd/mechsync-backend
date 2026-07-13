package com.mechsync.modules.customers.web.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
        @NotNull @Positive Long userId,
        @Size(max = 255)
        @Pattern(regexp = ".*\\S.*", message = "must not be blank")
        String address) {
}
