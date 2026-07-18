package com.mechsync.modules.jobs.domain.model;

public enum JobStatus {
    PENDIENTE,
    EN_PROCESO,
    COMPLETADO,
    CANCELADO;

    public boolean canTransitionTo(JobStatus target) {
        return switch (this) {
            case PENDIENTE -> target == EN_PROCESO || target == CANCELADO;
            case EN_PROCESO -> target == COMPLETADO || target == CANCELADO;
            case COMPLETADO, CANCELADO -> false;
        };
    }
}
