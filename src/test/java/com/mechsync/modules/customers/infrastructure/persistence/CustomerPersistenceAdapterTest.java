package com.mechsync.modules.customers.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.mechsync.modules.customers.infrastructure.repository.CustomerJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerPersistenceAdapterTest {

    @Mock
    private CustomerJpaRepository repository;

    private CustomerPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CustomerPersistenceAdapter(repository);
    }

    @Test
    void convertsUserCountToExistence() {
        when(repository.countUsersById(1L)).thenReturn(1L);
        when(repository.countUsersById(99L)).thenReturn(0L);

        assertTrue(adapter.userExists(1L));
        assertFalse(adapter.userExists(99L));
    }

    @Test
    void convertsVehicleCountToAssociationFlag() {
        when(repository.countVehiclesByCustomerId(1L)).thenReturn(2L);
        when(repository.countVehiclesByCustomerId(2L)).thenReturn(0L);

        assertTrue(adapter.hasVehicles(1L));
        assertFalse(adapter.hasVehicles(2L));
    }
}
