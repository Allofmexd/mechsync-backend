package com.mechsync.modules.workorders.application.port.in;

import com.mechsync.modules.workorders.application.dto.ApproveWorkOrderRevisionCommand;
import com.mechsync.modules.workorders.application.dto.RevisionActor;
import com.mechsync.modules.workorders.domain.model.WorkOrderRevision;

public interface WorkOrderRevisionWorkflowUseCase {
    WorkOrderRevision send(Long workOrderId, Long revisionId, RevisionActor actor);

    WorkOrderRevision approve(ApproveWorkOrderRevisionCommand command);

    WorkOrderRevision reject(Long workOrderId, Long revisionId, RevisionActor actor);

    WorkOrderRevision cancel(Long workOrderId, Long revisionId, RevisionActor actor);
}
