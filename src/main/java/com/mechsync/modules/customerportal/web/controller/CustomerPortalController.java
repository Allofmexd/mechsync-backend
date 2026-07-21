package com.mechsync.modules.customerportal.web.controller;

import com.mechsync.modules.customerportal.application.port.in.CustomerPortalQueryUseCase;
import com.mechsync.modules.customerportal.application.port.in.CustomerPortalOperationsUseCase;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalHistoryEvent;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalIntakeDetail;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalIntakeSummary;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalJobDetail;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalJobSummary;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalPage;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalProfile;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalQuotation;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalVehicleDetail;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalVehiclePage;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalWorkOrderDetail;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalWorkOrderSummary;
import com.mechsync.shared.web.ApiPaths;
import com.mechsync.shared.web.response.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(ApiPaths.CUSTOMER_PORTAL)
@PreAuthorize("hasRole('CLIENTE')")
public class CustomerPortalController {

    private final CustomerPortalQueryUseCase queryUseCase;
    private final CustomerPortalOperationsUseCase operationsUseCase;

    public CustomerPortalController(
            CustomerPortalQueryUseCase queryUseCase,
            CustomerPortalOperationsUseCase operationsUseCase) {
        this.queryUseCase = queryUseCase;
        this.operationsUseCase = operationsUseCase;
    }

    @GetMapping("/profile")
    public ApiResponse<CustomerPortalProfile> profile() {
        return ApiResponse.ok(queryUseCase.getProfile());
    }

    @GetMapping("/vehicles")
    public ApiResponse<CustomerPortalVehiclePage> vehicles(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponse.ok(queryUseCase.listVehicles(page, size));
    }

    @GetMapping("/vehicles/{vehicleId}")
    public ApiResponse<CustomerPortalVehicleDetail> vehicle(
            @PathVariable @Positive Long vehicleId) {
        return ApiResponse.ok(queryUseCase.getVehicle(vehicleId));
    }

    @GetMapping("/vehicle-intakes")
    public ApiResponse<CustomerPortalPage<CustomerPortalIntakeSummary>> intakes(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) @Positive Long vehicleId) {
        return ApiResponse.ok(operationsUseCase.listIntakes(page, size, vehicleId));
    }

    @GetMapping("/vehicle-intakes/{intakeId}")
    public ApiResponse<CustomerPortalIntakeDetail> intake(
            @PathVariable @Positive Long intakeId) {
        return ApiResponse.ok(operationsUseCase.getIntake(intakeId));
    }

    @GetMapping("/work-orders")
    public ApiResponse<CustomerPortalPage<CustomerPortalWorkOrderSummary>> workOrders(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) @Positive Long vehicleId,
            @RequestParam(required = false) @Positive Long intakeId,
            @RequestParam(defaultValue = "false") boolean quotationOnly) {
        return ApiResponse.ok(operationsUseCase.listWorkOrders(
                page, size, vehicleId, intakeId, quotationOnly));
    }

    @GetMapping("/work-orders/{workOrderId}")
    public ApiResponse<CustomerPortalWorkOrderDetail> workOrder(
            @PathVariable @Positive Long workOrderId) {
        return ApiResponse.ok(operationsUseCase.getWorkOrder(workOrderId));
    }

    @GetMapping("/work-orders/{workOrderId}/quotation")
    public ApiResponse<CustomerPortalQuotation> quotation(
            @PathVariable @Positive Long workOrderId) {
        return ApiResponse.ok(operationsUseCase.getQuotation(workOrderId));
    }

    @GetMapping("/jobs")
    public ApiResponse<CustomerPortalPage<CustomerPortalJobSummary>> jobs(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) @Positive Long vehicleId,
            @RequestParam(required = false) @Positive Long workOrderId) {
        return ApiResponse.ok(operationsUseCase.listJobs(page, size, vehicleId, workOrderId));
    }

    @GetMapping("/jobs/{jobId}")
    public ApiResponse<CustomerPortalJobDetail> job(
            @PathVariable @Positive Long jobId) {
        return ApiResponse.ok(operationsUseCase.getJob(jobId));
    }

    @GetMapping("/history")
    public ApiResponse<CustomerPortalPage<CustomerPortalHistoryEvent>> history(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) @Positive Long vehicleId) {
        return ApiResponse.ok(operationsUseCase.listHistory(page, size, vehicleId));
    }
}
