package com.mechsync.modules.vehicles.application.port.in;

import com.mechsync.modules.vehicles.application.dto.UpdateVehicleCommand;
import com.mechsync.modules.vehicles.domain.model.Vehicle;

public interface UpdateVehicleUseCase {
    Vehicle update(UpdateVehicleCommand command);
}
