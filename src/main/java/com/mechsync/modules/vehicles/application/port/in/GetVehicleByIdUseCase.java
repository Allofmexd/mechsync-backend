package com.mechsync.modules.vehicles.application.port.in;

import com.mechsync.modules.vehicles.domain.model.Vehicle;

public interface GetVehicleByIdUseCase {
    Vehicle getById(Long vehicleId);
}
