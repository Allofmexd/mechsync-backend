package com.mechsync.modules.customerportal.domain.exception;

public class CustomerPortalVehicleNotFoundException extends RuntimeException {

    public CustomerPortalVehicleNotFoundException() {
        super("Vehicle not found");
    }
}
