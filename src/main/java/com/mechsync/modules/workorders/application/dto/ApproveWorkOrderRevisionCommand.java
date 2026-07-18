package com.mechsync.modules.workorders.application.dto;

import java.time.LocalDateTime;

public record ApproveWorkOrderRevisionCommand(
        Long workOrderId,
        Long revisionId,
        RevisionActor actor,
        String acceptedByName,
        Long acceptedByUserId,
        LocalDateTime acceptedAt,
        String acceptanceMethod,
        String acceptanceNotes) {
}
