package com.mechsync.modules.workorders.application.usecase;

import com.mechsync.modules.workorders.application.dto.*;
import com.mechsync.modules.workorders.application.port.in.*;
import com.mechsync.modules.workorders.application.port.out.WorkOrderRepositoryPort;
import com.mechsync.modules.workorders.domain.exception.*;
import com.mechsync.modules.workorders.domain.model.WorkOrder;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional(readOnly=true)
public class WorkOrderService implements ListWorkOrdersUseCase,GetWorkOrderByIdUseCase,CreateWorkOrderUseCase,
        UpdateWorkOrderUseCase,DeleteWorkOrderUseCase {
    private final WorkOrderRepositoryPort repository;
    public WorkOrderService(WorkOrderRepositoryPort repository) { this.repository=repository; }
    @Override public WorkOrderPage list(int page,int size) { return repository.findAll(page,size); }
    @Override public WorkOrderPage listAssignedTo(Long technicianId,int page,int size) {
        return repository.findAllByTechnicianId(technicianId,page,size);
    }
    @Override public WorkOrder getById(Long id) { return repository.findById(id)
            .orElseThrow(() -> new WorkOrderNotFoundException(id)); }
    @Override public WorkOrder getAssignedTo(Long id,Long technicianId) {
        return repository.findByIdAndTechnicianId(id,technicianId)
                .orElseThrow(() -> new WorkOrderNotFoundException(id));
    }
    @Override @Transactional public WorkOrder create(CreateWorkOrderCommand c) {
        validateReferences(c.vehicleIntakeId(),c.technicianId(),c.statusId());
        validateDates(c.estimatedStartDate(),c.estimatedDeliveryDate());
        return repository.save(new WorkOrder(null,c.vehicleIntakeId(),c.technicianId(),
                c.workOrderDate()==null?LocalDateTime.now():c.workOrderDate(),c.estimatedStartDate(),
                c.estimatedDeliveryDate(),c.estimatedHours(),c.estimatedSubtotal(),c.estimatedIva(),
                c.estimatedTotal(),trimOptional(c.technicalObservations()),c.statusId(),null,null));
    }
    @Override @Transactional public WorkOrder update(UpdateWorkOrderCommand c) {
        WorkOrder current=getById(c.workOrderId());
        validateReferences(current.vehicleIntakeId(),c.technicianId(),c.statusId());
        validateDates(c.estimatedStartDate(),c.estimatedDeliveryDate());
        return repository.save(new WorkOrder(current.id(),current.vehicleIntakeId(),c.technicianId(),
                c.workOrderDate()==null?current.workOrderDate():c.workOrderDate(),c.estimatedStartDate(),
                c.estimatedDeliveryDate(),c.estimatedHours(),c.estimatedSubtotal(),c.estimatedIva(),
                c.estimatedTotal(),trimOptional(c.technicalObservations()),c.statusId(),current.createdAt(),
                LocalDateTime.now()));
    }
    @Override @Transactional public void delete(Long id) {
        getById(id); if(repository.hasDependencies(id)) throw new WorkOrderInUseException(id); repository.deleteById(id);
    }
    private void validateReferences(Long intake,Long technician,Long status) {
        if(!repository.vehicleIntakeExists(intake)) throw new WorkOrderVehicleIntakeNotFoundException(intake);
        if(!repository.technicianExists(technician)) throw new WorkOrderTechnicianNotFoundException(technician);
        if(!repository.workOrderStatusExists(status)) throw new WorkOrderStatusNotFoundException(status);
    }
    private void validateDates(LocalDateTime start,LocalDateTime delivery) {
        if(start!=null&&delivery!=null&&delivery.isBefore(start)) throw new InvalidWorkOrderDatesException();
    }
    private String trimOptional(String value) { return value==null?null:value.trim(); }
}
