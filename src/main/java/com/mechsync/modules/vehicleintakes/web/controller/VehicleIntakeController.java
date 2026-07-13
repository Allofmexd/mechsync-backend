package com.mechsync.modules.vehicleintakes.web.controller;

import com.mechsync.modules.vehicleintakes.application.dto.*;
import com.mechsync.modules.vehicleintakes.application.port.in.*;
import com.mechsync.modules.vehicleintakes.domain.model.VehicleIntake;
import com.mechsync.shared.web.ApiPaths;
import com.mechsync.shared.web.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController @Validated @RequestMapping(ApiPaths.VEHICLE_INTAKES)
public class VehicleIntakeController {
    private final ListVehicleIntakesUseCase listUseCase;
    private final GetVehicleIntakeByIdUseCase getUseCase;
    private final CreateVehicleIntakeUseCase createUseCase;
    private final UpdateVehicleIntakeUseCase updateUseCase;
    private final DeleteVehicleIntakeUseCase deleteUseCase;

    public VehicleIntakeController(ListVehicleIntakesUseCase listUseCase,
            GetVehicleIntakeByIdUseCase getUseCase, CreateVehicleIntakeUseCase createUseCase,
            UpdateVehicleIntakeUseCase updateUseCase, DeleteVehicleIntakeUseCase deleteUseCase) {
        this.listUseCase = listUseCase; this.getUseCase = getUseCase; this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase; this.deleteUseCase = deleteUseCase;
    }

    @GetMapping @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
    public ApiResponse<VehicleIntakePageResponse> list(@RequestParam(defaultValue="0") @Min(0) int page,
            @RequestParam(defaultValue="20") @Min(1) @Max(100) int size) {
        return ApiResponse.ok(VehicleIntakePageResponse.from(listUseCase.list(page, size)));
    }
    @GetMapping("/{id}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
    public ApiResponse<VehicleIntakeResponse> get(@PathVariable @Positive Long id) {
        return ApiResponse.ok(VehicleIntakeResponse.from(getUseCase.getById(id)));
    }
    @PostMapping @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
    public ResponseEntity<ApiResponse<VehicleIntakeResponse>> create(@Valid @RequestBody CreateVehicleIntakeRequest r) {
        VehicleIntake created = createUseCase.create(new CreateVehicleIntakeCommand(r.vehicleId(), r.technicianId(),
                r.intakeDate(), r.intakeMileage(), r.reportedProblem(), r.initialObservations(), r.statusId()));
        return ResponseEntity.created(URI.create(ApiPaths.VEHICLE_INTAKES + "/" + created.id()))
                .body(ApiResponse.ok(VehicleIntakeResponse.from(created)));
    }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
    public ApiResponse<VehicleIntakeResponse> update(@PathVariable @Positive Long id,
            @Valid @RequestBody UpdateVehicleIntakeRequest r) {
        return ApiResponse.ok(VehicleIntakeResponse.from(updateUseCase.update(new UpdateVehicleIntakeCommand(id,
                r.technicianId(), r.intakeDate(), r.intakeMileage(), r.reportedProblem(),
                r.initialObservations(), r.statusId()))));
    }
    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        deleteUseCase.delete(id); return ResponseEntity.noContent().build();
    }
}
