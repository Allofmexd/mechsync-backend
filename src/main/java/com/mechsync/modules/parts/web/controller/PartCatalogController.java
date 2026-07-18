package com.mechsync.modules.parts.web.controller;

import com.mechsync.modules.parts.application.port.in.ListPartsUseCase;
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
@RequestMapping(ApiPaths.PARTS)
public class PartCatalogController {

    private final ListPartsUseCase listPartsUseCase;

    public PartCatalogController(ListPartsUseCase listPartsUseCase) {
        this.listPartsUseCase = listPartsUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
    public ApiResponse<PartCatalogPageResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) @Size(max = 100) String search) {
        return ApiResponse.ok(PartCatalogPageResponse.from(
                listPartsUseCase.list(page, size, search)));
    }
}
