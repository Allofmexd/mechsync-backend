package com.mechsync.shared.infrastructure.security;

import com.mechsync.shared.web.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TestSecuredController {

    @GetMapping("/test/admin-only")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    ApiResponse<String> adminOnly() {
        return ApiResponse.ok("admin");
    }

    @GetMapping("/test/technician-only")
    @PreAuthorize("hasRole('TECNICO')")
    ApiResponse<String> technicianOnly() {
        return ApiResponse.ok("technician");
    }

    @GetMapping("/test/customer-only")
    @PreAuthorize("hasRole('CLIENTE')")
    ApiResponse<String> customerOnly() {
        return ApiResponse.ok("customer");
    }

    @GetMapping("/test/authenticated-only")
    @PreAuthorize("isAuthenticated()")
    ApiResponse<String> authenticatedOnly() {
        return ApiResponse.ok("authenticated");
    }
}
