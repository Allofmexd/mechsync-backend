package com.mechsync.modules.vehicleintakes.domain.exception;

public class VehicleIntakeTechnicianNotFoundException extends RuntimeException {
    public VehicleIntakeTechnicianNotFoundException(Long id) { super("Technician not found for intake: " + id); }
}
