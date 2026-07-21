package com.mechsync.modules.specialties.web.controller;

import com.mechsync.modules.specialties.application.port.in.ListSpecialtiesUseCase;
import com.mechsync.shared.web.ApiPaths;
import com.mechsync.shared.web.response.ApiResponse;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.SPECIALTIES)
public class SpecialtyController {

    private final ListSpecialtiesUseCase listSpecialtiesUseCase;

    public SpecialtyController(ListSpecialtiesUseCase listSpecialtiesUseCase) {
        this.listSpecialtiesUseCase = listSpecialtiesUseCase;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ApiResponse<List<SpecialtyResponse>> list() {
        return ApiResponse.ok(listSpecialtiesUseCase.list().stream()
                .map(SpecialtyResponse::from)
                .toList());
    }
}
