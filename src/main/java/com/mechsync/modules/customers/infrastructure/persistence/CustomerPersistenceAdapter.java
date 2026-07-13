package com.mechsync.modules.customers.infrastructure.persistence;

import com.mechsync.modules.customers.application.dto.CustomerPage;
import com.mechsync.modules.customers.application.port.out.CustomerRepositoryPort;
import com.mechsync.modules.customers.domain.exception.CustomerInUseException;
import com.mechsync.modules.customers.domain.exception.DuplicateCustomerException;
import com.mechsync.modules.customers.domain.model.Customer;
import com.mechsync.modules.customers.infrastructure.repository.CustomerJpaRepository;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class CustomerPersistenceAdapter implements CustomerRepositoryPort {

    private final CustomerJpaRepository repository;

    public CustomerPersistenceAdapter(CustomerJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public CustomerPage findAll(int page, int size) {
        Page<CustomerJpaEntity> result = repository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id")));
        return new CustomerPage(
                result.getContent().stream().map(this::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    public Optional<Customer> findById(Long customerId) {
        return repository.findById(customerId).map(this::toDomain);
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return repository.existsByUserId(userId);
    }

    @Override
    public boolean userExists(Long userId) {
        return repository.countUsersById(userId) > 0;
    }

    @Override
    public boolean hasVehicles(Long customerId) {
        return repository.countVehiclesByCustomerId(customerId) > 0;
    }

    @Override
    public Customer save(Customer customer) {
        try {
            CustomerJpaEntity saved = repository.saveAndFlush(new CustomerJpaEntity(
                    customer.id(),
                    customer.userId(),
                    customer.address(),
                    customer.registeredAt(),
                    customer.createdAt(),
                    customer.updatedAt()));
            return toDomain(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateCustomerException(customer.userId());
        }
    }

    @Override
    public void deleteById(Long customerId) {
        try {
            repository.deleteById(customerId);
            repository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new CustomerInUseException(customerId);
        }
    }

    private Customer toDomain(CustomerJpaEntity entity) {
        return new Customer(
                entity.getId(),
                entity.getUserId(),
                entity.getAddress(),
                entity.getRegisteredAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
