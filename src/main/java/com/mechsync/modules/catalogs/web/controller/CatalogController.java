package com.mechsync.modules.catalogs.web.controller;

import com.mechsync.modules.catalogs.application.port.in.ListCatalogStatusesUseCase;
import com.mechsync.shared.web.ApiPaths;
import com.mechsync.shared.web.response.ApiResponse;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.CATALOGS)
public class CatalogController {

    private final ListCatalogStatusesUseCase listCatalogStatusesUseCase;

    public CatalogController(ListCatalogStatusesUseCase listCatalogStatusesUseCase) {
        this.listCatalogStatusesUseCase = listCatalogStatusesUseCase;
    }

    @GetMapping("/statuses")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
    public ApiResponse<List<CatalogStatusResponse>> listStatuses(
            @RequestParam(defaultValue = "") String context) {
        return ApiResponse.ok(listCatalogStatusesUseCase.listByContext(context).stream()
                .map(CatalogStatusResponse::from)
                .toList());
    }
}
