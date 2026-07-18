package com.mechsync.modules.workorders.application.port.in;

import com.mechsync.modules.workorders.application.dto.RevisionActor;
import com.mechsync.modules.workorders.application.dto.WorkOrderRevisionPage;
import com.mechsync.modules.workorders.domain.model.WorkOrderRevision;

public interface WorkOrderRevisionQueryUseCase {
    WorkOrderRevisionPage list(Long workOrderId, int page, int size, RevisionActor actor);

    WorkOrderRevision get(Long workOrderId, Long revisionId, RevisionActor actor);

    WorkOrderRevision getCurrent(Long workOrderId, RevisionActor actor);

    WorkOrderRevision getFinalApproved(Long workOrderId, RevisionActor actor);
}
