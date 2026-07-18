package com.mechsync.modules.jobs.domain.exception;

public class JobTechnicianNotFoundException extends RuntimeException {
    public JobTechnicianNotFoundException(Long id) { super("Technician not found: " + id); }
}
