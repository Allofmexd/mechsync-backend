package com.mechsync.modules.vehicles.domain.exception;

public class InvalidVehicleYearException extends RuntimeException {
    public InvalidVehicleYearException(int minimum, int maximum) {
        super("Vehicle year must be between " + minimum + " and " + maximum);
    }
}
