package com.mechsync.modules.vehicles.domain.exception;

public class VehicleInUseException extends RuntimeException {
    public VehicleInUseException(Long vehicleId) {
        super("Vehicle cannot be deleted because it has associated intakes: " + vehicleId);
    }
}
