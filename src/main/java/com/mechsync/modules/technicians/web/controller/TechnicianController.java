package com.mechsync.modules.technicians.web.controller;

import com.mechsync.modules.technicians.application.dto.CreateTechnicianCommand;
import com.mechsync.modules.technicians.application.dto.UpdateTechnicianCommand;
import com.mechsync.modules.technicians.application.port.in.CreateTechnicianUseCase;
import com.mechsync.modules.technicians.application.port.in.GetTechnicianByIdUseCase;
import com.mechsync.modules.technicians.application.port.in.ListTechniciansUseCase;
import com.mechsync.modules.technicians.application.port.in.ResolveAuthenticatedTechnicianUseCase;
import com.mechsync.modules.technicians.application.port.in.UpdateTechnicianUseCase;
import com.mechsync.modules.technicians.domain.model.Technician;
import com.mechsync.shared.web.ApiPaths;
import com.mechsync.shared.web.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping(ApiPaths.TECHNICIANS)
public class TechnicianController {

    private final ListTechniciansUseCase listTechniciansUseCase;
    private final GetTechnicianByIdUseCase getTechnicianByIdUseCase;
    private final CreateTechnicianUseCase createTechnicianUseCase;
    private final UpdateTechnicianUseCase updateTechnicianUseCase;
    private final ResolveAuthenticatedTechnicianUseCase authenticatedTechnicianResolver;

    public TechnicianController(
            ListTechniciansUseCase listTechniciansUseCase,
            GetTechnicianByIdUseCase getTechnicianByIdUseCase,
            CreateTechnicianUseCase createTechnicianUseCase,
            UpdateTechnicianUseCase updateTechnicianUseCase,
            ResolveAuthenticatedTechnicianUseCase authenticatedTechnicianResolver) {
        this.listTechniciansUseCase = listTechniciansUseCase;
        this.getTechnicianByIdUseCase = getTechnicianByIdUseCase;
        this.createTechnicianUseCase = createTechnicianUseCase;
        this.updateTechnicianUseCase = updateTechnicianUseCase;
        this.authenticatedTechnicianResolver = authenticatedTechnicianResolver;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ApiResponse<List<TechnicianResponse>> list() {
        return ApiResponse.ok(listTechniciansUseCase.list().stream()
                .map(TechnicianResponse::from)
                .toList());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('TECNICO')")
    public ApiResponse<TechnicianResponse> me(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ApiResponse.ok(TechnicianResponse.from(
                authenticatedTechnicianResolver.resolve(authenticatedUser)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ApiResponse<TechnicianResponse> getById(@PathVariable @Positive Long id) {
        return ApiResponse.ok(TechnicianResponse.from(getTechnicianByIdUseCase.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<TechnicianResponse>> create(
            @Valid @RequestBody CreateTechnicianRequest request) {
        Technician created = createTechnicianUseCase.create(new CreateTechnicianCommand(
                request.userId(),
                request.specialtyId(),
                request.hireDate()));
        return ResponseEntity.created(URI.create(ApiPaths.TECHNICIANS + "/" + created.id()))
                .body(ApiResponse.ok(TechnicianResponse.from(created)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ApiResponse<TechnicianResponse> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateTechnicianRequest request) {
        Technician updated = updateTechnicianUseCase.update(new UpdateTechnicianCommand(
                id,
                request.specialtyId(),
                request.hireDate()));
        return ApiResponse.ok(TechnicianResponse.from(updated));
    }
}
