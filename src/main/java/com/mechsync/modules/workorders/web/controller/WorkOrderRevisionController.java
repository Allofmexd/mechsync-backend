package com.mechsync.modules.workorders.web.controller;

import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.workorders.application.dto.*;
import com.mechsync.modules.workorders.application.port.in.*;
import com.mechsync.modules.workorders.domain.model.WorkOrderRevision;
import com.mechsync.shared.web.ApiPaths;
import com.mechsync.shared.web.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping(ApiPaths.WORK_ORDER_REVISIONS)
public class WorkOrderRevisionController {

    private final WorkOrderRevisionQueryUseCase query;
    private final CreateWorkOrderRevisionUseCase create;
    private final WorkOrderRevisionWorkflowUseCase workflow;

    public WorkOrderRevisionController(
            WorkOrderRevisionQueryUseCase query,
            CreateWorkOrderRevisionUseCase create,
            WorkOrderRevisionWorkflowUseCase workflow) {
        this.query = query;
        this.create = create;
        this.workflow = workflow;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
    public ApiResponse<WorkOrderRevisionPageResponse> list(
            @PathVariable @Positive Long workOrderId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(WorkOrderRevisionPageResponse.from(
                query.list(workOrderId, page, size, actor(user))));
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
    public ApiResponse<WorkOrderRevisionResponse> current(
            @PathVariable @Positive Long workOrderId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(WorkOrderRevisionResponse.from(
                query.getCurrent(workOrderId, actor(user))));
    }

    @GetMapping("/final-approved")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
    public ApiResponse<WorkOrderRevisionResponse> finalApproved(
            @PathVariable @Positive Long workOrderId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(WorkOrderRevisionResponse.from(
                query.getFinalApproved(workOrderId, actor(user))));
    }

    @GetMapping("/{revisionId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
    public ApiResponse<WorkOrderRevisionResponse> get(
            @PathVariable @Positive Long workOrderId,
            @PathVariable @Positive Long revisionId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(WorkOrderRevisionResponse.from(
                query.get(workOrderId, revisionId, actor(user))));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<WorkOrderRevisionResponse>> create(
            @PathVariable @Positive Long workOrderId,
            @Valid @RequestBody CreateWorkOrderRevisionRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        WorkOrderRevision revision = create.create(toCommand(workOrderId, request, actor(user)));
        URI location = URI.create(ApiPaths.WORK_ORDERS + "/" + workOrderId
                + "/revisions/" + revision.id());
        return ResponseEntity.created(location)
                .body(ApiResponse.ok(WorkOrderRevisionResponse.from(revision)));
    }

    @PatchMapping("/{revisionId}/send")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ApiResponse<WorkOrderRevisionResponse> send(
            @PathVariable @Positive Long workOrderId,
            @PathVariable @Positive Long revisionId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(WorkOrderRevisionResponse.from(
                workflow.send(workOrderId, revisionId, actor(user))));
    }

    @PatchMapping("/{revisionId}/approve")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ApiResponse<WorkOrderRevisionResponse> approve(
            @PathVariable @Positive Long workOrderId,
            @PathVariable @Positive Long revisionId,
            @Valid @RequestBody ApproveWorkOrderRevisionRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(WorkOrderRevisionResponse.from(workflow.approve(
                new ApproveWorkOrderRevisionCommand(
                        workOrderId, revisionId, actor(user), request.acceptedByName(),
                        request.acceptedByUserId(), request.acceptedAt(), request.acceptanceMethod(),
                        request.acceptanceNotes()))));
    }

    @PatchMapping("/{revisionId}/reject")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ApiResponse<WorkOrderRevisionResponse> reject(
            @PathVariable @Positive Long workOrderId,
            @PathVariable @Positive Long revisionId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(WorkOrderRevisionResponse.from(
                workflow.reject(workOrderId, revisionId, actor(user))));
    }

    @PatchMapping("/{revisionId}/cancel")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ApiResponse<WorkOrderRevisionResponse> cancel(
            @PathVariable @Positive Long workOrderId,
            @PathVariable @Positive Long revisionId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(WorkOrderRevisionResponse.from(
                workflow.cancel(workOrderId, revisionId, actor(user))));
    }

    private RevisionActor actor(AuthenticatedUser user) {
        return new RevisionActor(user.id(), user.roles());
    }

    private CreateWorkOrderRevisionCommand toCommand(
            Long workOrderId,
            CreateWorkOrderRevisionRequest request,
            RevisionActor actor) {
        List<CreateWorkOrderRevisionRequest.ServiceLine> serviceRequests =
                request.services() == null ? List.of() : request.services();
        List<CreateWorkOrderRevisionRequest.PartLine> partRequests =
                request.parts() == null ? List.of() : request.parts();
        List<CreateRevisionServiceLineCommand> services = java.util.stream.IntStream
                .range(0, serviceRequests.size())
                .mapToObj(index -> {
                    CreateWorkOrderRevisionRequest.ServiceLine line = serviceRequests.get(index);
                    return new CreateRevisionServiceLineCommand(
                            line.lineNumber() == null ? index + 1 : line.lineNumber(),
                            line.serviceId(), line.nameSnapshot(), line.descriptionSnapshot(),
                            line.quantity(), line.unitPrice(), line.lineSubtotal(), line.notes());
                }).toList();
        List<CreateRevisionPartLineCommand> parts = java.util.stream.IntStream
                .range(0, partRequests.size())
                .mapToObj(index -> {
                    CreateWorkOrderRevisionRequest.PartLine line = partRequests.get(index);
                    return new CreateRevisionPartLineCommand(
                            line.lineNumber() == null ? index + 1 : line.lineNumber(),
                            line.partId(), line.nameSnapshot(), line.descriptionSnapshot(),
                            line.quantity(), line.unitPrice(), line.lineSubtotal(), line.notes());
                }).toList();
        return new CreateWorkOrderRevisionCommand(
                workOrderId, actor, request.technicianId(), request.estimatedStartDate(),
                request.estimatedDeliveryDate(), request.estimatedHours(), request.currency(),
                request.applyIva(), request.ivaRate(), request.subtotalAmount(), request.ivaAmount(),
                request.totalAmount(), request.taxTreatmentNotes(), request.technicalObservations(),
                request.customerNotes(), request.changeReason(), services, parts);
    }
}
