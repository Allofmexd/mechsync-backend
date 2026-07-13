package com.mechsync.modules.workorders.application.usecase;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.mechsync.modules.workorders.application.dto.*;
import com.mechsync.modules.workorders.application.port.out.WorkOrderRepositoryPort;
import com.mechsync.modules.workorders.domain.exception.*;
import com.mechsync.modules.workorders.domain.model.WorkOrder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
class WorkOrderServiceTest {
 @Mock WorkOrderRepositoryPort repository; WorkOrderService service;
 @BeforeEach void setup(){service=new WorkOrderService(repository);}
 @Test void createsValidPlanningOrder(){validReferences();when(repository.save(any())).thenAnswer(i->{WorkOrder o=i.getArgument(0);
  return new WorkOrder(1L,o.vehicleIntakeId(),o.technicianId(),o.workOrderDate(),o.estimatedStartDate(),
   o.estimatedDeliveryDate(),o.estimatedHours(),o.estimatedSubtotal(),o.estimatedIva(),o.estimatedTotal(),
   o.technicalObservations(),o.statusId(),LocalDateTime.now(),null);});
  WorkOrder result=service.create(create());assertEquals(1L,result.id());assertEquals("Planning",result.technicalObservations());}
 @Test void rejectsMissingIntake(){when(repository.vehicleIntakeExists(99L)).thenReturn(false);
  assertThrows(WorkOrderVehicleIntakeNotFoundException.class,()->service.create(command(99L,2L,12L)));}
 @Test void rejectsMissingTechnician(){when(repository.vehicleIntakeExists(1L)).thenReturn(true);
  assertThrows(WorkOrderTechnicianNotFoundException.class,()->service.create(command(1L,99L,12L)));}
 @Test void rejectsStatusOutsideContext(){when(repository.vehicleIntakeExists(1L)).thenReturn(true);
  when(repository.technicianExists(2L)).thenReturn(true);
  assertThrows(WorkOrderStatusNotFoundException.class,()->service.create(command(1L,2L,7L)));}
 @Test void missingOrderThrowsNotFound(){when(repository.findById(99L)).thenReturn(Optional.empty());
  assertThrows(WorkOrderNotFoundException.class,()->service.getById(99L));}
 @Test void updatePreservesIntake(){when(repository.findById(1L)).thenReturn(Optional.of(order()));validReferences();
  when(repository.save(any())).thenAnswer(i->i.getArgument(0));WorkOrder result=service.update(new UpdateWorkOrderCommand(
   1L,2L,null,null,null,new BigDecimal("3.00"),new BigDecimal("200.00"),new BigDecimal("32.00"),
   new BigDecimal("232.00"),"Updated",13L));assertEquals(1L,result.vehicleIntakeId());}
 @Test void rejectsDeliveryBeforeStart(){validReferences();LocalDateTime start=LocalDateTime.of(2026,7,20,10,0);
  CreateWorkOrderCommand c=new CreateWorkOrderCommand(1L,2L,null,start,start.minusDays(1),BigDecimal.ONE,
   BigDecimal.ZERO,BigDecimal.ZERO,BigDecimal.ZERO,null,12L);
  assertThrows(InvalidWorkOrderDatesException.class,()->service.create(c));}
 @Test void deletesWithoutDependencies(){when(repository.findById(1L)).thenReturn(Optional.of(order()));service.delete(1L);
  verify(repository).deleteById(1L);}
 @Test void refusesDeleteWithDependencies(){when(repository.findById(1L)).thenReturn(Optional.of(order()));
  when(repository.hasDependencies(1L)).thenReturn(true);assertThrows(WorkOrderInUseException.class,()->service.delete(1L));
  verify(repository,never()).deleteById(1L);}
 private void validReferences(){when(repository.vehicleIntakeExists(1L)).thenReturn(true);
  when(repository.technicianExists(2L)).thenReturn(true);when(repository.workOrderStatusExists(any())).thenReturn(true);}
 private CreateWorkOrderCommand create(){return command(1L,2L,12L);}
 private CreateWorkOrderCommand command(Long intake,Long tech,Long status){return new CreateWorkOrderCommand(intake,tech,null,
  null,null,new BigDecimal("2.00"),new BigDecimal("100.00"),new BigDecimal("16.00"),
  new BigDecimal("116.00")," Planning ",status);}
 private WorkOrder order(){return new WorkOrder(1L,1L,2L,LocalDateTime.of(2026,7,12,10,0),null,null,
  new BigDecimal("2.00"),new BigDecimal("100.00"),new BigDecimal("16.00"),new BigDecimal("116.00"),
  "Planning",12L,LocalDateTime.of(2026,7,12,10,0),null);}
}
