package com.mechsync.modules.workorders.application.dto;

import java.util.Set;

public record RevisionActor(Long userId, Set<String> roles) {
    public RevisionActor {
        roles = Set.copyOf(roles);
    }

    public boolean isAdministrator() {
        return roles.contains("ADMINISTRADOR");
    }

    public boolean isTechnician() {
        return roles.contains("TECNICO");
    }
}
