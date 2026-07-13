package com.mechsync.modules.customers.application.port.in;

import com.mechsync.modules.customers.application.dto.UpdateCustomerCommand;
import com.mechsync.modules.customers.domain.model.Customer;

public interface UpdateCustomerUseCase {

    Customer update(UpdateCustomerCommand command);
}
