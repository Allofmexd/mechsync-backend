package com.mechsync.modules.vehicleintakes.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import com.mechsync.modules.vehicleintakes.infrastructure.repository.VehicleIntakeJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleIntakePersistenceAdapterTest {
    @Mock VehicleIntakeJpaRepository repository;
    @Test void translatesReferenceAndDependencyCounts() {
        when(repository.countVehiclesById(1L)).thenReturn(1L);
        when(repository.countTechniciansById(2L)).thenReturn(1L);
        when(repository.countIntakeStatusesById(7L)).thenReturn(1L);
        when(repository.countWorkOrdersByIntakeId(3L)).thenReturn(1L);
        VehicleIntakePersistenceAdapter adapter=new VehicleIntakePersistenceAdapter(repository);
        assertTrue(adapter.vehicleExists(1L)); assertTrue(adapter.technicianExists(2L));
        assertTrue(adapter.intakeStatusExists(7L)); assertTrue(adapter.hasWorkOrders(3L));
        assertFalse(adapter.vehicleExists(99L));
    }
}
