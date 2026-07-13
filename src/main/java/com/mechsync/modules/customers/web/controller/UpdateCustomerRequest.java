package com.mechsync.modules.customers.web.controller;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(
        @Size(max = 255)
        @Pattern(regexp = ".*\\S.*", message = "must not be blank")
        String address) {
}
