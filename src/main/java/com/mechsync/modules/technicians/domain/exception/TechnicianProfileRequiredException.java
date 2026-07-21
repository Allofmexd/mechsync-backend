package com.mechsync.modules.technicians.domain.exception;

public class TechnicianProfileRequiredException extends RuntimeException {

    public TechnicianProfileRequiredException() {
        super("Authenticated technician profile is required");
    }
}
