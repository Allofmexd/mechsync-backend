package com.mechsync.modules.dashboard.web.controller;

import com.mechsync.modules.dashboard.application.port.in.DashboardQueryUseCase;
import com.mechsync.modules.dashboard.application.usecase.DashboardPeriodResolver;
import com.mechsync.modules.dashboard.domain.model.DashboardPeriod;
import com.mechsync.shared.web.ApiPaths;
import com.mechsync.shared.web.response.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping(ApiPaths.DASHBOARD)
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class DashboardController {
    private final DashboardQueryUseCase dashboard;
    private final DashboardPeriodResolver periods;

    public DashboardController(DashboardQueryUseCase dashboard,
            DashboardPeriodResolver periods) {
        this.dashboard = dashboard;
        this.periods = periods;
    }

    @GetMapping("/summary")
    public ApiResponse<DashboardResponses.Summary> summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate to) {
        return ApiResponse.ok(DashboardResponses.Summary.from(
                dashboard.summary(periods.resolve(from, to))));
    }

    @GetMapping("/work-orders-by-status")
    public ApiResponse<List<DashboardResponses.Status>> workOrdersByStatus(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate to) {
        DashboardPeriod period = periods.resolve(from, to);
        return ApiResponse.ok(dashboard.workOrdersByStatus(period).stream()
                .map(DashboardResponses.Status::from).toList());
    }

    @GetMapping("/jobs-by-status")
    public ApiResponse<List<DashboardResponses.Status>> jobsByStatus(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate to) {
        DashboardPeriod period = periods.resolve(from, to);
        return ApiResponse.ok(dashboard.jobsByStatus(period).stream()
                .map(DashboardResponses.Status::from).toList());
    }

    @GetMapping("/revenue-by-month")
    public ApiResponse<List<DashboardResponses.Revenue>> revenueByMonth(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate to) {
        DashboardPeriod period = periods.resolve(from, to);
        return ApiResponse.ok(dashboard.revenueByMonth(period).stream()
                .map(DashboardResponses.Revenue::from).toList());
    }

    @GetMapping("/top-services")
    public ApiResponse<List<DashboardResponses.TopService>> topServices(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate to,
            @RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit) {
        DashboardPeriod period = periods.resolve(from, to);
        return ApiResponse.ok(dashboard.topServices(period, limit).stream()
                .map(DashboardResponses.TopService::from).toList());
    }

    @GetMapping("/technician-workload")
    public ApiResponse<List<DashboardResponses.TechnicianWorkload>> technicianWorkload(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate to) {
        DashboardPeriod period = periods.resolve(from, to);
        return ApiResponse.ok(dashboard.technicianWorkload(period).stream()
                .map(DashboardResponses.TechnicianWorkload::from).toList());
    }
}
