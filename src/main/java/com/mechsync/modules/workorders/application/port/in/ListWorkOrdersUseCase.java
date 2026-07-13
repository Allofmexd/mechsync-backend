package com.mechsync.modules.workorders.application.port.in;
import com.mechsync.modules.workorders.application.dto.WorkOrderPage;
public interface ListWorkOrdersUseCase { WorkOrderPage list(int page,int size); }
