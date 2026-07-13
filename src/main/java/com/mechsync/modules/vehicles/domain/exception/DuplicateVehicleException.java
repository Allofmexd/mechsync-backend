package com.mechsync.modules.vehicles.domain.exception;

public class DuplicateVehicleException extends RuntimeException {
    public DuplicateVehicleException() {
        super("A vehicle already exists with the same license plate or VIN");
    }
}
