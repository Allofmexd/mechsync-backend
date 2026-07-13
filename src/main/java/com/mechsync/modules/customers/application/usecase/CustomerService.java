package com.mechsync.modules.customers.application.usecase;

import com.mechsync.modules.customers.application.dto.CreateCustomerCommand;
import com.mechsync.modules.customers.application.dto.CustomerPage;
import com.mechsync.modules.customers.application.dto.UpdateCustomerCommand;
import com.mechsync.modules.customers.application.port.in.CreateCustomerUseCase;
import com.mechsync.modules.customers.application.port.in.DeleteCustomerUseCase;
import com.mechsync.modules.customers.application.port.in.GetCustomerByIdUseCase;
import com.mechsync.modules.customers.application.port.in.ListCustomersUseCase;
import com.mechsync.modules.customers.application.port.in.UpdateCustomerUseCase;
import com.mechsync.modules.customers.application.port.out.CustomerRepositoryPort;
import com.mechsync.modules.customers.domain.exception.CustomerInUseException;
import com.mechsync.modules.customers.domain.exception.CustomerNotFoundException;
import com.mechsync.modules.customers.domain.exception.CustomerUserNotFoundException;
import com.mechsync.modules.customers.domain.exception.DuplicateCustomerException;
import com.mechsync.modules.customers.domain.model.Customer;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerService implements
        ListCustomersUseCase,
        GetCustomerByIdUseCase,
        CreateCustomerUseCase,
        UpdateCustomerUseCase,
        DeleteCustomerUseCase {

    private final CustomerRepositoryPort customerRepository;

    public CustomerService(CustomerRepositoryPort customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public CustomerPage list(int page, int size) {
        return customerRepository.findAll(page, size);
    }

    @Override
    public Customer getById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    @Override
    @Transactional
    public Customer create(CreateCustomerCommand command) {
        if (!customerRepository.userExists(command.userId())) {
            throw new CustomerUserNotFoundException(command.userId());
        }
        if (customerRepository.existsByUserId(command.userId())) {
            throw new DuplicateCustomerException(command.userId());
        }
        return customerRepository.save(new Customer(
                null,
                command.userId(),
                normalizeAddress(command.address()),
                null,
                null,
                null));
    }

    @Override
    @Transactional
    public Customer update(UpdateCustomerCommand command) {
        Customer current = getById(command.customerId());
        Customer updated = new Customer(
                current.id(),
                current.userId(),
                normalizeAddress(command.address()),
                current.registeredAt(),
                current.createdAt(),
                LocalDateTime.now());
        return customerRepository.save(updated);
    }

    @Override
    @Transactional
    public void delete(Long customerId) {
        getById(customerId);
        if (customerRepository.hasVehicles(customerId)) {
            throw new CustomerInUseException(customerId);
        }
        customerRepository.deleteById(customerId);
    }

    private String normalizeAddress(String address) {
        return address == null ? null : address.trim();
    }
}
