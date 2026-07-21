package com.mechsync.modules.workorders.infrastructure.persistence;
import com.mechsync.modules.workorders.application.dto.WorkOrderPage;
import com.mechsync.modules.workorders.application.port.out.WorkOrderRepositoryPort;
import com.mechsync.modules.workorders.domain.exception.WorkOrderInUseException;
import com.mechsync.modules.workorders.domain.model.WorkOrder;
import com.mechsync.modules.workorders.infrastructure.repository.WorkOrderJpaRepository;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;
@Component
public class WorkOrderPersistenceAdapter implements WorkOrderRepositoryPort {
 private final WorkOrderJpaRepository repository;
 public WorkOrderPersistenceAdapter(WorkOrderJpaRepository repository){this.repository=repository;}
 @Override public WorkOrderPage findAll(int page,int size){Page<WorkOrderJpaEntity> r=repository.findAll(
  PageRequest.of(page,size,Sort.by(Sort.Direction.ASC,"id")));return new WorkOrderPage(
  r.getContent().stream().map(this::toDomain).toList(),r.getNumber(),r.getSize(),r.getTotalElements(),r.getTotalPages());}
 @Override public WorkOrderPage findAllByTechnicianId(Long technicianId,int page,int size){
  Page<WorkOrderJpaEntity> r=repository.findAllByTechnicianId(technicianId,
   PageRequest.of(page,size,Sort.by(Sort.Direction.ASC,"id")));return new WorkOrderPage(
   r.getContent().stream().map(this::toDomain).toList(),r.getNumber(),r.getSize(),r.getTotalElements(),r.getTotalPages());}
 @Override public Optional<WorkOrder> findById(Long id){return repository.findById(id).map(this::toDomain);}
 @Override public Optional<WorkOrder> findByIdAndTechnicianId(Long id,Long technicianId){
  return repository.findByIdAndTechnicianId(id,technicianId).map(this::toDomain);}
 @Override public boolean vehicleIntakeExists(Long id){return repository.countVehicleIntakesById(id)>0;}
 @Override public boolean technicianExists(Long id){return repository.countTechniciansById(id)>0;}
 @Override public boolean workOrderStatusExists(Long id){return repository.countWorkOrderStatusesById(id)>0;}
 @Override public boolean hasDependencies(Long id){return repository.countDependenciesByWorkOrderId(id)>0;}
 @Override public WorkOrder save(WorkOrder o){
  WorkOrderJpaEntity entity;
  if(o.id()==null){entity=new WorkOrderJpaEntity(null,o.vehicleIntakeId(),o.technicianId(),o.workOrderDate(),
   o.estimatedStartDate(),o.estimatedDeliveryDate(),o.estimatedHours(),o.estimatedSubtotal(),o.estimatedIva(),
   o.estimatedTotal(),o.technicalObservations(),o.statusId(),o.createdAt(),o.updatedAt());}
  else{entity=repository.findById(o.id()).orElseThrow(()->new com.mechsync.modules.workorders.domain.exception.WorkOrderNotFoundException(o.id()));
   entity.updateLegacyFields(o.technicianId(),o.workOrderDate(),o.estimatedStartDate(),o.estimatedDeliveryDate(),
    o.estimatedHours(),o.estimatedSubtotal(),o.estimatedIva(),o.estimatedTotal(),o.technicalObservations(),
    o.statusId(),o.updatedAt());}
  return toDomain(repository.saveAndFlush(entity));}
 @Override public void deleteById(Long id){try{repository.deleteById(id);repository.flush();}
  catch(DataIntegrityViolationException e){throw new WorkOrderInUseException(id);}}
 private WorkOrder toDomain(WorkOrderJpaEntity e){return new WorkOrder(e.getId(),e.getVehicleIntakeId(),
  e.getTechnicianId(),e.getWorkOrderDate(),e.getEstimatedStartDate(),e.getEstimatedDeliveryDate(),
  e.getEstimatedHours(),e.getEstimatedSubtotal(),e.getEstimatedIva(),e.getEstimatedTotal(),
  e.getTechnicalObservations(),e.getStatusId(),e.getCreatedAt(),e.getUpdatedAt());}
}
