package com.mechsync.modules.customers.application.port.in;

import com.mechsync.modules.customers.domain.model.AuthenticatedCustomer;

public interface ResolveAuthenticatedCustomerUseCase {

    AuthenticatedCustomer resolve();

    default Long resolveId() {
        return resolve().customerId();
    }
}
