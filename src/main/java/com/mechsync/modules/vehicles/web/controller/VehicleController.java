package com.mechsync.modules.vehicles.web.controller;

import com.mechsync.modules.vehicles.application.dto.CreateVehicleCommand;
import com.mechsync.modules.vehicles.application.dto.UpdateVehicleCommand;
import com.mechsync.modules.vehicles.application.port.in.CreateVehicleUseCase;
import com.mechsync.modules.vehicles.application.port.in.DeleteVehicleUseCase;
import com.mechsync.modules.vehicles.application.port.in.GetVehicleByIdUseCase;
import com.mechsync.modules.vehicles.application.port.in.ListVehiclesUseCase;
import com.mechsync.modules.vehicles.application.port.in.UpdateVehicleUseCase;
import com.mechsync.modules.vehicles.domain.model.Vehicle;
import com.mechsync.shared.web.ApiPaths;
import com.mechsync.shared.web.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping(ApiPaths.VEHICLES)
public class VehicleController {

    private final ListVehiclesUseCase listVehiclesUseCase;
    private final GetVehicleByIdUseCase getVehicleByIdUseCase;
    private final CreateVehicleUseCase createVehicleUseCase;
    private final UpdateVehicleUseCase updateVehicleUseCase;
    private final DeleteVehicleUseCase deleteVehicleUseCase;

    public VehicleController(
            ListVehiclesUseCase listVehiclesUseCase,
            GetVehicleByIdUseCase getVehicleByIdUseCase,
            CreateVehicleUseCase createVehicleUseCase,
            UpdateVehicleUseCase updateVehicleUseCase,
            DeleteVehicleUseCase deleteVehicleUseCase) {
        this.listVehiclesUseCase = listVehiclesUseCase;
        this.getVehicleByIdUseCase = getVehicleByIdUseCase;
        this.createVehicleUseCase = createVehicleUseCase;
        this.updateVehicleUseCase = updateVehicleUseCase;
        this.deleteVehicleUseCase = deleteVehicleUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
    public ApiResponse<VehiclePageResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponse.ok(VehiclePageResponse.from(listVehiclesUseCase.list(page, size)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','TECNICO')")
    public ApiResponse<VehicleResponse> getById(@PathVariable @Positive Long id) {
        return ApiResponse.ok(VehicleResponse.from(getVehicleByIdUseCase.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<VehicleResponse>> create(
            @Valid @RequestBody CreateVehicleRequest request) {
        Vehicle created = createVehicleUseCase.create(new CreateVehicleCommand(
                request.customerId(), request.brand(), request.model(), request.year(), request.color(),
                request.licensePlate(), request.vin(), request.currentMileage()));
        return ResponseEntity.created(URI.create(ApiPaths.VEHICLES + "/" + created.id()))
                .body(ApiResponse.ok(VehicleResponse.from(created)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ApiResponse<VehicleResponse> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateVehicleRequest request) {
        Vehicle updated = updateVehicleUseCase.update(new UpdateVehicleCommand(
                id, request.brand(), request.model(), request.year(), request.color(),
                request.licensePlate(), request.vin(), request.currentMileage()));
        return ApiResponse.ok(VehicleResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        deleteVehicleUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
