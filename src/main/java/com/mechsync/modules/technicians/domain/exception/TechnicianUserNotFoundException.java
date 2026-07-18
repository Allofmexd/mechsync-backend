package com.mechsync.modules.technicians.domain.exception;

public class TechnicianUserNotFoundException extends RuntimeException {

    public TechnicianUserNotFoundException(Long userId) {
        super("Technician user not found: " + userId);
    }
}
