package com.mechsync.modules.vehicles.application.port.in;

import com.mechsync.modules.vehicles.application.dto.CreateVehicleCommand;
import com.mechsync.modules.vehicles.domain.model.Vehicle;

public interface CreateVehicleUseCase {
    Vehicle create(CreateVehicleCommand command);
}
