package com.mechsync.modules.technicians.domain.exception;

public class TechnicianSpecialtyNotFoundException extends RuntimeException {

    public TechnicianSpecialtyNotFoundException(Long specialtyId) {
        super("Technician specialty not found: " + specialtyId);
    }
}
