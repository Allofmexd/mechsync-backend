package com.mechsync.modules.servicereports.web.controller;

import com.mechsync.modules.servicereports.application.dto.CreateServiceReportCommand;
import com.mechsync.modules.servicereports.application.dto.GeneratedServiceReportPdf;
import com.mechsync.modules.servicereports.application.port.in.*;
import com.mechsync.modules.servicereports.domain.model.ServiceReport;
import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.technicians.application.port.in.ResolveAuthenticatedTechnicianUseCase;
import com.mechsync.shared.web.ApiPaths;
import com.mechsync.shared.web.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.net.URI;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
public class ServiceReportController {
    private final ServiceReportQueryUseCase query;
    private final CreateServiceReportUseCase create;
    private final GenerateServiceReportPdfUseCase pdf;
    private final ResolveAuthenticatedTechnicianUseCase technicianResolver;

    public ServiceReportController(ServiceReportQueryUseCase query,
            CreateServiceReportUseCase create, GenerateServiceReportPdfUseCase pdf,
            ResolveAuthenticatedTechnicianUseCase technicianResolver) {
        this.query = query;
        this.create = create;
        this.pdf = pdf;
        this.technicianResolver = technicianResolver;
    }

    @GetMapping(ApiPaths.SERVICE_REPORTS)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ApiResponse<ServiceReportPageResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponse.ok(ServiceReportPageResponse.from(query.list(page, size)));
    }

    @GetMapping(ApiPaths.SERVICE_REPORTS + "/assigned-to-me")
    @PreAuthorize("hasRole('TECNICO')")
    public ApiResponse<ServiceReportPageResponse> assignedToMe(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(ServiceReportPageResponse.from(query.listAssignedTo(
                technicianResolver.resolveId(user), page, size)));
    }

    @GetMapping(ApiPaths.SERVICE_REPORTS + "/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
    public ApiResponse<ServiceReportResponse> get(@PathVariable @Positive Long id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        ServiceReport report = user.roles().contains("ADMINISTRADOR")
                ? query.get(id)
                : query.getAssignedTo(id, technicianResolver.resolveId(user));
        return ApiResponse.ok(ServiceReportResponse.from(report));
    }

    @GetMapping(value = ApiPaths.SERVICE_REPORTS + "/{id}/pdf",
            produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable @Positive Long id,
            @AuthenticationPrincipal AuthenticatedUser user) {
        GeneratedServiceReportPdf generated = user.roles().contains("ADMINISTRADOR")
                ? pdf.generate(id)
                : pdf.generateAssignedTo(id, technicianResolver.resolveId(user));
        byte[] content = generated.content();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(content.length)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + generated.filename() + "\"")
                .cacheControl(CacheControl.noStore())
                .body(content);
    }

    @GetMapping(ApiPaths.JOBS + "/{jobId}/service-report")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
    public ApiResponse<ServiceReportResponse> getByJob(@PathVariable @Positive Long jobId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        ServiceReport report = user.roles().contains("ADMINISTRADOR")
                ? query.getByJobId(jobId)
                : query.getByJobIdAssignedTo(jobId, technicianResolver.resolveId(user));
        return ApiResponse.ok(ServiceReportResponse.from(report));
    }

    @PostMapping(ApiPaths.SERVICE_REPORTS)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<ServiceReportResponse>> create(
            @Valid @RequestBody CreateServiceReportRequest request) {
        ServiceReport report = create.create(new CreateServiceReportCommand(request.jobId(),
                request.finalDescription(), request.customerConfirmation(),
                request.deliveredAt()));
        return ResponseEntity.created(URI.create(ApiPaths.SERVICE_REPORTS + "/" + report.id()))
                .body(ApiResponse.ok(ServiceReportResponse.from(report)));
    }
}
