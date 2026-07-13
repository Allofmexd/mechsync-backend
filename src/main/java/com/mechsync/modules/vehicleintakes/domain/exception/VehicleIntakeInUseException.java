package com.mechsync.modules.vehicleintakes.domain.exception;

public class VehicleIntakeInUseException extends RuntimeException {
    public VehicleIntakeInUseException(Long id) { super("Vehicle intake has dependent work orders: " + id); }
}
