package com.mechsync.modules.customers.application.port.out;

import com.mechsync.modules.customers.application.dto.CustomerPage;
import com.mechsync.modules.customers.domain.model.Customer;
import java.util.Optional;

public interface CustomerRepositoryPort {

    CustomerPage findAll(int page, int size);

    Optional<Customer> findById(Long customerId);

    boolean existsByUserId(Long userId);

    boolean userExists(Long userId);

    boolean hasVehicles(Long customerId);

    Customer save(Customer customer);

    void deleteById(Long customerId);
}
