package com.mechsync.modules.workorders.infrastructure.persistence;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import com.mechsync.modules.workorders.infrastructure.repository.WorkOrderJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
class WorkOrderPersistenceAdapterTest {
 @Mock WorkOrderJpaRepository repository;
 @Test void translatesCounts(){when(repository.countVehicleIntakesById(1L)).thenReturn(1L);
  when(repository.countTechniciansById(2L)).thenReturn(1L);when(repository.countWorkOrderStatusesById(12L)).thenReturn(1L);
  when(repository.countDependenciesByWorkOrderId(3L)).thenReturn(1L);WorkOrderPersistenceAdapter a=new WorkOrderPersistenceAdapter(repository);
  assertTrue(a.vehicleIntakeExists(1L));assertTrue(a.technicianExists(2L));assertTrue(a.workOrderStatusExists(12L));
  assertTrue(a.hasDependencies(3L));assertFalse(a.vehicleIntakeExists(99L));}
}
