package com.mechsync.modules.services.web.controller;

import com.mechsync.modules.services.application.port.in.ListServicesUseCase;
import com.mechsync.shared.web.ApiPaths;
import com.mechsync.shared.web.response.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(ApiPaths.SERVICES)
public class ServiceCatalogController {

    private final ListServicesUseCase listServicesUseCase;

    public ServiceCatalogController(ListServicesUseCase listServicesUseCase) {
        this.listServicesUseCase = listServicesUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
    public ApiResponse<ServiceCatalogPageResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) @Size(max = 100) String search) {
        return ApiResponse.ok(ServiceCatalogPageResponse.from(
                listServicesUseCase.list(page, size, search)));
    }
}
