package com.mechsync.modules.vehicleintakes.application.port.in;
import com.mechsync.modules.vehicleintakes.application.dto.UpdateVehicleIntakeCommand;
import com.mechsync.modules.vehicleintakes.domain.model.VehicleIntake;
public interface UpdateVehicleIntakeUseCase { VehicleIntake update(UpdateVehicleIntakeCommand command); }
