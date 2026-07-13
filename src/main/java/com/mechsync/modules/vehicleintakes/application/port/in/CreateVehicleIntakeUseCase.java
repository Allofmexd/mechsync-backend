package com.mechsync.modules.vehicleintakes.application.port.in;
import com.mechsync.modules.vehicleintakes.application.dto.CreateVehicleIntakeCommand;
import com.mechsync.modules.vehicleintakes.domain.model.VehicleIntake;
public interface CreateVehicleIntakeUseCase { VehicleIntake create(CreateVehicleIntakeCommand command); }
