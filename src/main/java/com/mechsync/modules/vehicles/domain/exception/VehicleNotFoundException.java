package com.mechsync.modules.vehicles.domain.exception;

public class VehicleNotFoundException extends RuntimeException {
    public VehicleNotFoundException(Long vehicleId) {
        super("Vehicle not found: " + vehicleId);
    }
}
