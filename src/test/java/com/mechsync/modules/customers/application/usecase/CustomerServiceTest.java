package com.mechsync.modules.customers.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mechsync.modules.customers.application.dto.CreateCustomerCommand;
import com.mechsync.modules.customers.application.dto.UpdateCustomerCommand;
import com.mechsync.modules.customers.application.port.out.CustomerRepositoryPort;
import com.mechsync.modules.customers.domain.exception.CustomerInUseException;
import com.mechsync.modules.customers.domain.exception.CustomerNotFoundException;
import com.mechsync.modules.customers.domain.exception.CustomerUserNotFoundException;
import com.mechsync.modules.customers.domain.exception.CustomerUserRoleRequiredException;
import com.mechsync.modules.customers.domain.exception.DuplicateCustomerException;
import com.mechsync.modules.customers.domain.model.Customer;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepositoryPort repository;

    private CustomerService service;

    @BeforeEach
    void setUp() {
        service = new CustomerService(repository);
    }

    @Test
    void createsCustomerForExistingUser() {
        when(repository.userExists(2L)).thenReturn(true);
        when(repository.userHasRole(2L, "CLIENTE")).thenReturn(true);
        when(repository.existsByUserId(2L)).thenReturn(false);
        when(repository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            return new Customer(10L, customer.userId(), customer.address(),
                    LocalDateTime.now(), LocalDateTime.now(), null);
        });

        Customer result = service.create(new CreateCustomerCommand(2L, "  Main Street  "));

        assertEquals(10L, result.id());
        assertEquals("Main Street", result.address());
    }

    @Test
    void rejectsDuplicateCustomerForSameUser() {
        when(repository.userExists(2L)).thenReturn(true);
        when(repository.userHasRole(2L, "CLIENTE")).thenReturn(true);
        when(repository.existsByUserId(2L)).thenReturn(true);

        assertThrows(DuplicateCustomerException.class,
                () -> service.create(new CreateCustomerCommand(2L, null)));
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsMissingUserBeforeCheckingRole() {
        when(repository.userExists(99L)).thenReturn(false);

        assertThrows(CustomerUserNotFoundException.class,
                () -> service.create(new CreateCustomerCommand(99L, null)));
        verify(repository, never()).userHasRole(99L, "CLIENTE");
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rejectsUserWithoutCustomerRole() {
        when(repository.userExists(2L)).thenReturn(true);
        when(repository.userHasRole(2L, "CLIENTE")).thenReturn(false);

        assertThrows(CustomerUserRoleRequiredException.class,
                () -> service.create(new CreateCustomerCommand(2L, null)));
        verify(repository, never()).existsByUserId(2L);
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void returnsExistingCustomer() {
        Customer customer = customer();
        when(repository.findById(1L)).thenReturn(Optional.of(customer));

        assertEquals(customer, service.getById(1L));
    }

    @Test
    void missingCustomerThrowsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> service.getById(99L));
    }

    @Test
    void updatesAddressWithoutChangingUserAssociation() {
        when(repository.findById(1L)).thenReturn(Optional.of(customer()));
        when(repository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = service.update(new UpdateCustomerCommand(1L, "  New Address "));

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(repository).save(captor.capture());
        assertEquals(2L, captor.getValue().userId());
        assertEquals("New Address", result.address());
        assertTrue(result.updatedAt() != null);
    }

    @Test
    void deletesCustomerWithoutVehicles() {
        when(repository.findById(1L)).thenReturn(Optional.of(customer()));
        when(repository.hasVehicles(1L)).thenReturn(false);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void refusesDeletionWhenCustomerHasVehicles() {
        when(repository.findById(1L)).thenReturn(Optional.of(customer()));
        when(repository.hasVehicles(1L)).thenReturn(true);

        assertThrows(CustomerInUseException.class, () -> service.delete(1L));
        verify(repository, never()).deleteById(1L);
    }

    private Customer customer() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 1, 1, 12, 0);
        return new Customer(1L, 2L, "Main Street", timestamp, timestamp, null);
    }
}
