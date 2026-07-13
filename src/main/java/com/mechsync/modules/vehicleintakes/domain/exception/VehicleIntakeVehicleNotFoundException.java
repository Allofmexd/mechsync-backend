package com.mechsync.modules.vehicleintakes.domain.exception;

public class VehicleIntakeVehicleNotFoundException extends RuntimeException {
    public VehicleIntakeVehicleNotFoundException(Long id) { super("Vehicle not found for intake: " + id); }
}
