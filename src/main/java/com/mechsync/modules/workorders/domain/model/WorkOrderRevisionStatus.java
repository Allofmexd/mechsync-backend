package com.mechsync.modules.workorders.domain.model;

import java.util.Set;

public enum WorkOrderRevisionStatus {
    DRAFT,
    SENT,
    APPROVED,
    REJECTED,
    SUPERSEDED,
    CANCELLED;

    public boolean canTransitionTo(WorkOrderRevisionStatus target) {
        return switch (this) {
            case DRAFT -> Set.of(SENT, CANCELLED, SUPERSEDED).contains(target);
            case SENT -> Set.of(APPROVED, REJECTED, CANCELLED, SUPERSEDED).contains(target);
            case REJECTED -> target == SUPERSEDED;
            case APPROVED, SUPERSEDED, CANCELLED -> false;
        };
    }

    public boolean isSupersededWhenReplaced() {
        return this == DRAFT || this == SENT || this == REJECTED;
    }
}
