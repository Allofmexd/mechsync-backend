package com.mechsync.modules.customers.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.mechsync.modules.customers.infrastructure.repository.CustomerJpaRepository;
import com.mechsync.modules.customers.domain.exception.CustomerIntegrityException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

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
    void findsCustomerDirectlyByUserId() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 7, 21, 12, 0);
        when(repository.findByUserId(7L)).thenReturn(Optional.of(new CustomerJpaEntity(
                3L, 7L, "Address", timestamp, timestamp, null)));

        assertEquals(3L, adapter.findByUserId(7L).orElseThrow().id());
    }

    @Test
    void returnsEmptyWhenUserHasNoCustomer() {
        when(repository.findByUserId(7L)).thenReturn(Optional.empty());

        assertTrue(adapter.findByUserId(7L).isEmpty());
    }

    @Test
    void convertsDuplicateRelationshipToControlledIntegrityFailure() {
        when(repository.findByUserId(7L))
                .thenThrow(new IncorrectResultSizeDataAccessException(1, 2));

        assertThrows(CustomerIntegrityException.class, () -> adapter.findByUserId(7L));
    }

    @Test
    void verifiesCustomerRoleThroughDirectCount() {
        when(repository.countUserRoles(7L, "CLIENTE")).thenReturn(1L);
        when(repository.countUserRoles(8L, "CLIENTE")).thenReturn(0L);

        assertTrue(adapter.userHasRole(7L, "CLIENTE"));
        assertFalse(adapter.userHasRole(8L, "CLIENTE"));
    }

    @Test
    void convertsVehicleCountToAssociationFlag() {
        when(repository.countVehiclesByCustomerId(1L)).thenReturn(2L);
        when(repository.countVehiclesByCustomerId(2L)).thenReturn(0L);

        assertTrue(adapter.hasVehicles(1L));
        assertFalse(adapter.hasVehicles(2L));
    }
}
