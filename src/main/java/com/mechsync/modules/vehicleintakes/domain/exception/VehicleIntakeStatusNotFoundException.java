package com.mechsync.modules.vehicleintakes.domain.exception;

public class VehicleIntakeStatusNotFoundException extends RuntimeException {
    public VehicleIntakeStatusNotFoundException(Long id) {
        super("Vehicle intake status not found or invalid for VEHICLE_INTAKES: " + id);
    }
}
