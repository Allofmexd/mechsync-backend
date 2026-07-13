package com.mechsync.modules.workorders.application.port.in;
import com.mechsync.modules.workorders.application.dto.UpdateWorkOrderCommand;
import com.mechsync.modules.workorders.domain.model.WorkOrder;
public interface UpdateWorkOrderUseCase { WorkOrder update(UpdateWorkOrderCommand command); }
