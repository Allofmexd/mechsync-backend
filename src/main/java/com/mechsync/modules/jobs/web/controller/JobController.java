package com.mechsync.modules.jobs.web.controller;

import com.mechsync.modules.jobs.application.dto.*;
import com.mechsync.modules.jobs.application.port.in.*;
import com.mechsync.modules.jobs.domain.model.Job;
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
@RequestMapping(ApiPaths.JOBS)
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class JobController {
    private final JobQueryUseCase query;
    private final CreateJobUseCase create;
    private final JobWorkflowUseCase workflow;

    public JobController(JobQueryUseCase query, CreateJobUseCase create,
            JobWorkflowUseCase workflow) {
        this.query = query;
        this.create = create;
        this.workflow = workflow;
    }

    @GetMapping
    public ApiResponse<JobPageResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponse.ok(JobPageResponse.from(query.list(page, size)));
    }

    @GetMapping("/{id}")
    public ApiResponse<JobResponse> get(@PathVariable @Positive Long id) {
        return ApiResponse.ok(JobResponse.from(query.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobResponse>> create(
            @Valid @RequestBody CreateJobRequest request) {
        Job job = create.create(new CreateJobCommand(request.workOrderId(),
                request.initialApprovedRevisionId(), request.technicianId(),
                request.scheduledStartDate(), request.notes()));
        return ResponseEntity.created(URI.create(ApiPaths.JOBS + "/" + job.id()))
                .body(ApiResponse.ok(JobResponse.from(job)));
    }

    @PatchMapping("/{id}/start")
    public ApiResponse<JobResponse> start(@PathVariable @Positive Long id) {
        return ApiResponse.ok(JobResponse.from(workflow.start(id)));
    }

    @PatchMapping("/{id}/complete")
    public ApiResponse<JobResponse> complete(@PathVariable @Positive Long id,
            @Valid @RequestBody CompleteJobRequest request) {
        return ApiResponse.ok(JobResponse.from(workflow.complete(new CompleteJobCommand(id,
                request.realSubtotalAmount(), request.realIvaAmount(),
                request.realTotalAmount(), request.notes()))));
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<JobResponse> cancel(@PathVariable @Positive Long id,
            @Valid @RequestBody CancelJobRequest request) {
        return ApiResponse.ok(JobResponse.from(
                workflow.cancel(new CancelJobCommand(id, request.cancellationNotes()))));
    }
}
