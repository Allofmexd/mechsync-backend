package com.mechsync.modules.technicians.domain.exception;

public class TechnicianUserRoleRequiredException extends RuntimeException {

    public TechnicianUserRoleRequiredException(Long userId) {
        super("User must have TECNICO role to create a technician profile: " + userId);
    }
}
