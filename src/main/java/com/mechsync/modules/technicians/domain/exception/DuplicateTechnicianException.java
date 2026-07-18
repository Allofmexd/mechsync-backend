package com.mechsync.modules.technicians.domain.exception;

public class DuplicateTechnicianException extends RuntimeException {

    public DuplicateTechnicianException(Long userId) {
        super("Technician profile already exists for user: " + userId);
    }
}
