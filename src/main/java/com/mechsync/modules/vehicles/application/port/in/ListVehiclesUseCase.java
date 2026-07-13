package com.mechsync.modules.vehicles.application.port.in;

import com.mechsync.modules.vehicles.application.dto.VehiclePage;

public interface ListVehiclesUseCase {
    VehiclePage list(int page, int size);
}
