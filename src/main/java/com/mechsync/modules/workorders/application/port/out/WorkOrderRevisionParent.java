package com.mechsync.modules.workorders.application.port.out;

public record WorkOrderRevisionParent(
        Long workOrderId,
        Long currentRevisionId,
        Long finalApprovedRevisionId,
        long lockVersion) {
}
