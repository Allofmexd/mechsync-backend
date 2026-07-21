package com.mechsync.modules.jobs.web.controller;

import com.mechsync.modules.jobs.application.dto.UpsertJobPartLineCommand;
import com.mechsync.modules.jobs.application.dto.UpsertJobServiceLineCommand;
import com.mechsync.modules.jobs.application.port.in.JobLineUseCase;
import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.jobs.domain.model.JobPartLine;
import com.mechsync.modules.jobs.domain.model.JobServiceLine;
import com.mechsync.modules.technicians.application.port.in.ResolveAuthenticatedTechnicianUseCase;
import com.mechsync.shared.web.ApiPaths;
import com.mechsync.shared.web.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(ApiPaths.JOBS + "/{jobId}")
public class JobLineController {
    private final JobLineUseCase useCase;
    private final ResolveAuthenticatedTechnicianUseCase technicianResolver;

    public JobLineController(JobLineUseCase useCase,
            ResolveAuthenticatedTechnicianUseCase technicianResolver) {
        this.useCase = useCase;
        this.technicianResolver = technicianResolver;
    }

    @GetMapping("/services")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
    public ApiResponse<List<JobServiceLineResponse>> listServices(
            @PathVariable @Positive Long jobId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        List<JobServiceLine> lines = user.roles().contains("ADMINISTRADOR")
                ? useCase.listServices(jobId)
                : useCase.listServicesAssignedTo(jobId, technicianResolver.resolveId(user));
        return ApiResponse.ok(lines.stream()
                .map(JobServiceLineResponse::from).toList());
    }

    @PostMapping("/services")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<JobServiceLineResponse>> addService(
            @PathVariable @Positive Long jobId,
            @Valid @RequestBody JobServiceLineRequest request) {
        JobServiceLine line = useCase.addService(serviceCommand(jobId, null, request));
        return ResponseEntity.created(URI.create(ApiPaths.JOBS + "/" + jobId
                + "/services/" + line.id()))
                .body(ApiResponse.ok(JobServiceLineResponse.from(line)));
    }

    @PutMapping("/services/{lineId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ApiResponse<JobServiceLineResponse> updateService(
            @PathVariable @Positive Long jobId,
            @PathVariable @Positive Long lineId,
            @Valid @RequestBody JobServiceLineRequest request) {
        return ApiResponse.ok(JobServiceLineResponse.from(
                useCase.updateService(serviceCommand(jobId, lineId, request))));
    }

    @DeleteMapping("/services/{lineId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> deleteService(
            @PathVariable @Positive Long jobId,
            @PathVariable @Positive Long lineId) {
        useCase.deleteService(jobId, lineId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/parts")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
    public ApiResponse<List<JobPartLineResponse>> listParts(
            @PathVariable @Positive Long jobId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        List<JobPartLine> lines = user.roles().contains("ADMINISTRADOR")
                ? useCase.listParts(jobId)
                : useCase.listPartsAssignedTo(jobId, technicianResolver.resolveId(user));
        return ApiResponse.ok(lines.stream()
                .map(JobPartLineResponse::from).toList());
    }

    @PostMapping("/parts")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<JobPartLineResponse>> addPart(
            @PathVariable @Positive Long jobId,
            @Valid @RequestBody JobPartLineRequest request) {
        JobPartLine line = useCase.addPart(partCommand(jobId, null, request));
        return ResponseEntity.created(URI.create(ApiPaths.JOBS + "/" + jobId
                + "/parts/" + line.id()))
                .body(ApiResponse.ok(JobPartLineResponse.from(line)));
    }

    @PutMapping("/parts/{lineId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ApiResponse<JobPartLineResponse> updatePart(
            @PathVariable @Positive Long jobId,
            @PathVariable @Positive Long lineId,
            @Valid @RequestBody JobPartLineRequest request) {
        return ApiResponse.ok(JobPartLineResponse.from(
                useCase.updatePart(partCommand(jobId, lineId, request))));
    }

    @DeleteMapping("/parts/{lineId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> deletePart(
            @PathVariable @Positive Long jobId,
            @PathVariable @Positive Long lineId) {
        useCase.deletePart(jobId, lineId);
        return ResponseEntity.noContent().build();
    }

    private UpsertJobServiceLineCommand serviceCommand(Long jobId, Long lineId,
            JobServiceLineRequest request) {
        return new UpsertJobServiceLineCommand(jobId, lineId, request.serviceId(),
                request.quantity(), request.unitPrice());
    }

    private UpsertJobPartLineCommand partCommand(Long jobId, Long lineId,
            JobPartLineRequest request) {
        return new UpsertJobPartLineCommand(jobId, lineId, request.partId(),
                request.quantity(), request.unitPrice());
    }
}
