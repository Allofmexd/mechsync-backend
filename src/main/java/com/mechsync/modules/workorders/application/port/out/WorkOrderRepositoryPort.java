package com.mechsync.modules.workorders.application.port.out;
import com.mechsync.modules.workorders.application.dto.WorkOrderPage;
import com.mechsync.modules.workorders.domain.model.WorkOrder;
import java.util.Optional;
public interface WorkOrderRepositoryPort {
    WorkOrderPage findAll(int page,int size);
    WorkOrderPage findAllByTechnicianId(Long technicianId,int page,int size);
    Optional<WorkOrder> findById(Long id);
    Optional<WorkOrder> findByIdAndTechnicianId(Long id,Long technicianId);
    boolean vehicleIntakeExists(Long id); boolean technicianExists(Long id); boolean workOrderStatusExists(Long id);
    boolean hasDependencies(Long id); WorkOrder save(WorkOrder order); void deleteById(Long id);
}
