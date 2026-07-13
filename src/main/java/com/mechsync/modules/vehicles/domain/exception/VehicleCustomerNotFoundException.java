package com.mechsync.modules.vehicles.domain.exception;

public class VehicleCustomerNotFoundException extends RuntimeException {
    public VehicleCustomerNotFoundException(Long customerId) {
        super("Customer not found: " + customerId);
    }
}
