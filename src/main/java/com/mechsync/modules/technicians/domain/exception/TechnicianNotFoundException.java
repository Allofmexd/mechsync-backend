package com.mechsync.modules.technicians.domain.exception;

public class TechnicianNotFoundException extends RuntimeException {

    public TechnicianNotFoundException(Long technicianId) {
        super("Technician not found: " + technicianId);
    }
}
