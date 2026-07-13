package com.mechsync.modules.vehicleintakes.application.port.in;
import com.mechsync.modules.vehicleintakes.application.dto.VehicleIntakePage;
public interface ListVehicleIntakesUseCase { VehicleIntakePage list(int page, int size); }
