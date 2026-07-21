package com.mechsync.modules.customers.web.controller;

import com.mechsync.modules.customers.application.port.in.ResolveAuthenticatedCustomerUseCase;
import com.mechsync.modules.customers.domain.model.AuthenticatedCustomer;
import com.mechsync.shared.web.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test/customer-portal")
class TestCustomerPortalSecurityController {

    private final ResolveAuthenticatedCustomerUseCase resolver;

    TestCustomerPortalSecurityController(ResolveAuthenticatedCustomerUseCase resolver) {
        this.resolver = resolver;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    ApiResponse<AuthenticatedCustomer> me() {
        return ApiResponse.ok(resolver.resolve());
    }
}
