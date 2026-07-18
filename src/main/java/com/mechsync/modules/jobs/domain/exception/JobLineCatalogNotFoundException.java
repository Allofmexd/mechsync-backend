package com.mechsync.modules.jobs.domain.exception;

public class JobLineCatalogNotFoundException extends RuntimeException {
    public JobLineCatalogNotFoundException(String catalogType, Long catalogId) {
        super(catalogType + " " + catalogId + " was not found");
    }
}
