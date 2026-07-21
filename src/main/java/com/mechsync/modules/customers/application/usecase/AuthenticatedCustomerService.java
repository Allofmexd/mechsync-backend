package com.mechsync.modules.customers.application.usecase;

import com.mechsync.modules.auth.application.port.out.CurrentAuthenticatedUserPort;
import com.mechsync.modules.auth.domain.model.AuthenticatedUser;
import com.mechsync.modules.customers.application.port.in.ResolveAuthenticatedCustomerUseCase;
import com.mechsync.modules.customers.application.port.out.CustomerRepositoryPort;
import com.mechsync.modules.customers.domain.exception.CustomerIntegrityException;
import com.mechsync.modules.customers.domain.exception.CustomerPortalAccessDeniedException;
import com.mechsync.modules.customers.domain.exception.CustomerProfileRequiredException;
import com.mechsync.modules.customers.domain.model.AuthenticatedCustomer;
import com.mechsync.modules.customers.domain.model.Customer;
import com.mechsync.shared.domain.constant.SystemRole;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthenticatedCustomerService implements ResolveAuthenticatedCustomerUseCase {

    private final CurrentAuthenticatedUserPort currentAuthenticatedUser;
    private final CustomerRepositoryPort customerRepository;

    public AuthenticatedCustomerService(
            CurrentAuthenticatedUserPort currentAuthenticatedUser,
            CustomerRepositoryPort customerRepository) {
        this.currentAuthenticatedUser = currentAuthenticatedUser;
        this.customerRepository = customerRepository;
    }

    @Override
    public AuthenticatedCustomer resolve() {
        AuthenticatedUser user = currentAuthenticatedUser.getCurrentUser();
        if (user == null || user.id() == null) {
            throw new AuthenticationCredentialsNotFoundException("Authentication required");
        }
        if (!user.roles().contains(SystemRole.CLIENTE.name())) {
            throw new CustomerPortalAccessDeniedException();
        }

        Customer customer = customerRepository.findByUserId(user.id())
                .orElseThrow(CustomerProfileRequiredException::new);
        if (customer.id() == null || !user.id().equals(customer.userId())) {
            throw new CustomerIntegrityException();
        }
        return new AuthenticatedCustomer(customer.id());
    }
}
