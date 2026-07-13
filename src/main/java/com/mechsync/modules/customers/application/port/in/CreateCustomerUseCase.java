package com.mechsync.modules.customers.application.port.in;

import com.mechsync.modules.customers.application.dto.CreateCustomerCommand;
import com.mechsync.modules.customers.domain.model.Customer;

public interface CreateCustomerUseCase {

    Customer create(CreateCustomerCommand command);
}
