package com.mechsync.modules.workorders.application.port.in;

import com.mechsync.modules.workorders.application.dto.CreateWorkOrderRevisionCommand;
import com.mechsync.modules.workorders.domain.model.WorkOrderRevision;

public interface CreateWorkOrderRevisionUseCase {
    WorkOrderRevision create(CreateWorkOrderRevisionCommand command);
}
