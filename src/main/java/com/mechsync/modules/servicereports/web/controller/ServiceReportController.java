package com.mechsync.modules.servicereports.web.controller;

import com.mechsync.modules.servicereports.application.dto.CreateServiceReportCommand;
import com.mechsync.modules.servicereports.application.port.in.*;
import com.mechsync.modules.servicereports.domain.model.ServiceReport;
import com.mechsync.shared.web.ApiPaths;
import com.mechsync.shared.web.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class ServiceReportController {
    private final ServiceReportQueryUseCase query;
    private final CreateServiceReportUseCase create;

    public ServiceReportController(ServiceReportQueryUseCase query,
            CreateServiceReportUseCase create) {
        this.query = query;
        this.create = create;
    }

    @GetMapping(ApiPaths.SERVICE_REPORTS)
    public ApiResponse<ServiceReportPageResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponse.ok(ServiceReportPageResponse.from(query.list(page, size)));
    }

    @GetMapping(ApiPaths.SERVICE_REPORTS + "/{id}")
    public ApiResponse<ServiceReportResponse> get(@PathVariable @Positive Long id) {
        return ApiResponse.ok(ServiceReportResponse.from(query.get(id)));
    }

    @GetMapping(ApiPaths.JOBS + "/{jobId}/service-report")
    public ApiResponse<ServiceReportResponse> getByJob(@PathVariable @Positive Long jobId) {
        return ApiResponse.ok(ServiceReportResponse.from(query.getByJobId(jobId)));
    }

    @PostMapping(ApiPaths.SERVICE_REPORTS)
    public ResponseEntity<ApiResponse<ServiceReportResponse>> create(
            @Valid @RequestBody CreateServiceReportRequest request) {
        ServiceReport report = create.create(new CreateServiceReportCommand(request.jobId(),
                request.finalDescription(), request.customerConfirmation(),
                request.deliveredAt()));
        return ResponseEntity.created(URI.create(ApiPaths.SERVICE_REPORTS + "/" + report.id()))
                .body(ApiResponse.ok(ServiceReportResponse.from(report)));
    }
}
