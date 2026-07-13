package com.mechsync.modules.customers.application.port.in;

import com.mechsync.modules.customers.domain.model.Customer;

public interface GetCustomerByIdUseCase {

    Customer getById(Long customerId);
}
