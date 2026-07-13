package com.mechsync.modules.vehicleintakes.domain.exception;

public class VehicleIntakeNotFoundException extends RuntimeException {
    public VehicleIntakeNotFoundException(Long id) { super("Vehicle intake not found: " + id); }
}
