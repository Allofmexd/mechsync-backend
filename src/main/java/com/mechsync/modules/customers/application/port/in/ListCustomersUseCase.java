package com.mechsync.modules.customers.application.port.in;

import com.mechsync.modules.customers.application.dto.CustomerPage;

public interface ListCustomersUseCase {

    CustomerPage list(int page, int size);
}
