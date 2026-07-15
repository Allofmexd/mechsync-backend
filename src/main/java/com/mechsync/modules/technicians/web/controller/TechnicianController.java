package com.mechsync.modules.technicians.web.controller;

import com.mechsync.modules.technicians.application.port.in.ListTechniciansUseCase;
import com.mechsync.shared.web.ApiPaths;
import com.mechsync.shared.web.response.ApiResponse;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.TECHNICIANS)
public class TechnicianController {

    private final ListTechniciansUseCase listTechniciansUseCase;

    public TechnicianController(ListTechniciansUseCase listTechniciansUseCase) {
        this.listTechniciansUseCase = listTechniciansUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
    public ApiResponse<List<TechnicianResponse>> list() {
        return ApiResponse.ok(listTechniciansUseCase.list().stream()
                .map(TechnicianResponse::from)
                .toList());
    }
}
