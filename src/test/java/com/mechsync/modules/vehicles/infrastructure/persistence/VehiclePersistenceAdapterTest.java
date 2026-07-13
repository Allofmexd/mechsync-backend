package com.mechsync.modules.vehicles.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.mechsync.modules.vehicles.infrastructure.repository.VehicleJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehiclePersistenceAdapterTest {

    @Mock private VehicleJpaRepository repository;
    private VehiclePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new VehiclePersistenceAdapter(repository);
    }

    @Test
    void convertsCustomerCountToExistence() {
        when(repository.countCustomersById(1L)).thenReturn(1L);
        when(repository.countCustomersById(99L)).thenReturn(0L);

        assertTrue(adapter.customerExists(1L));
        assertFalse(adapter.customerExists(99L));
    }

    @Test
    void convertsIntakeCountToDependencyFlag() {
        when(repository.countIntakesByVehicleId(1L)).thenReturn(2L);
        when(repository.countIntakesByVehicleId(2L)).thenReturn(0L);

        assertTrue(adapter.hasIntakes(1L));
        assertFalse(adapter.hasIntakes(2L));
    }
}
