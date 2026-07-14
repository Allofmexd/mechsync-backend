package com.mechsync.shared.infrastructure.security;

import com.mechsync.shared.web.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
class TestSecuredController {

    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    ApiResponse<String> adminOnly() {
        return ApiResponse.ok("admin");
    }

    @GetMapping("/technician-only")
    @PreAuthorize("hasRole('TECNICO')")
    ApiResponse<String> technicianOnly() {
        return ApiResponse.ok("technician");
    }

    @GetMapping("/customer-only")
    @PreAuthorize("hasRole('CLIENTE')")
    ApiResponse<String> customerOnly() {
        return ApiResponse.ok("customer");
    }

    @GetMapping("/authenticated-only")
    @PreAuthorize("isAuthenticated()")
    ApiResponse<String> authenticatedOnly() {
        return ApiResponse.ok("authenticated");
    }
}
