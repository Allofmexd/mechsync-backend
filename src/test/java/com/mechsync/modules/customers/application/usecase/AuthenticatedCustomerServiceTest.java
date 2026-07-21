package com.mechsync.modules.customers.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mechsync.modules.auth.application.port.out.CurrentAuthenticatedUserPort;
import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.customers.application.port.out.CustomerRepositoryPort;
import com.mechsync.modules.customers.domain.exception.CustomerIntegrityException;
import com.mechsync.modules.customers.domain.exception.CustomerPortalAccessDeniedException;
import com.mechsync.modules.customers.domain.exception.CustomerProfileRequiredException;
import com.mechsync.modules.customers.domain.model.AuthenticatedCustomer;
import com.mechsync.modules.customers.domain.model.Customer;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

@ExtendWith(MockitoExtension.class)
class AuthenticatedCustomerServiceTest {

    @Mock
    private CurrentAuthenticatedUserPort currentAuthenticatedUser;

    @Mock
    private CustomerRepositoryPort customerRepository;

    private AuthenticatedCustomerService service;

    @BeforeEach
    void setUp() {
        service = new AuthenticatedCustomerService(currentAuthenticatedUser, customerRepository);
    }

    @Test
    void resolvesMinimalCustomerIdentityForAuthenticatedCustomerRole() {
        when(currentAuthenticatedUser.getCurrentUser()).thenReturn(customerUser());
        when(customerRepository.findByUserId(7L)).thenReturn(Optional.of(customer(3L, 7L)));

        AuthenticatedCustomer result = service.resolve();

        assertEquals(3L, result.customerId());
        assertEquals(1, AuthenticatedCustomer.class.getRecordComponents().length);
    }

    @Test
    void customerWithoutProfileIsRejected() {
        when(currentAuthenticatedUser.getCurrentUser()).thenReturn(customerUser());
        when(customerRepository.findByUserId(7L)).thenReturn(Optional.empty());

        assertThrows(CustomerProfileRequiredException.class, service::resolve);
    }

    @Test
    void technicianIsRejectedBeforeCustomerLookup() {
        when(currentAuthenticatedUser.getCurrentUser()).thenReturn(new AuthenticatedUser(
                7L, "tech@mechsync.local", Set.of("TECNICO")));

        assertThrows(CustomerPortalAccessDeniedException.class, service::resolve);
        verify(customerRepository, never()).findByUserId(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void administratorIsRejectedBeforeCustomerLookup() {
        when(currentAuthenticatedUser.getCurrentUser()).thenReturn(new AuthenticatedUser(
                1L, "admin@mechsync.local", Set.of("ADMINISTRADOR")));

        assertThrows(CustomerPortalAccessDeniedException.class, service::resolve);
        verify(customerRepository, never()).findByUserId(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void missingUserIdIsRejectedAsMissingAuthentication() {
        when(currentAuthenticatedUser.getCurrentUser()).thenReturn(new AuthenticatedUser(
                null, "customer@mechsync.local", Set.of("CLIENTE")));

        assertThrows(AuthenticationCredentialsNotFoundException.class, service::resolve);
        verify(customerRepository, never()).findByUserId(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void inconsistentCustomerRelationshipIsRejected() {
        when(currentAuthenticatedUser.getCurrentUser()).thenReturn(customerUser());
        when(customerRepository.findByUserId(7L)).thenReturn(Optional.of(customer(3L, 8L)));

        assertThrows(CustomerIntegrityException.class, service::resolve);
    }

    @Test
    void customerWithoutPersistentIdIsRejected() {
        when(currentAuthenticatedUser.getCurrentUser()).thenReturn(customerUser());
        when(customerRepository.findByUserId(7L)).thenReturn(Optional.of(customer(null, 7L)));

        assertThrows(CustomerIntegrityException.class, service::resolve);
    }

    private AuthenticatedUser customerUser() {
        return new AuthenticatedUser(7L, "customer@mechsync.local", Set.of("CLIENTE"));
    }

    private Customer customer(Long customerId, Long userId) {
        LocalDateTime timestamp = LocalDateTime.of(2026, 7, 21, 12, 0);
        return new Customer(customerId, userId, null, timestamp, timestamp, null);
    }
}
