package com.mechsync.modules.workorders.application.port.in;
import com.mechsync.modules.workorders.domain.model.WorkOrder;
public interface GetWorkOrderByIdUseCase {
    WorkOrder getById(Long id);
    WorkOrder getAssignedTo(Long id,Long technicianId);
}
