package com.mechsync.modules.workorders.application.port.in;
import com.mechsync.modules.workorders.application.dto.CreateWorkOrderCommand;
import com.mechsync.modules.workorders.domain.model.WorkOrder;
public interface CreateWorkOrderUseCase { WorkOrder create(CreateWorkOrderCommand command); }
