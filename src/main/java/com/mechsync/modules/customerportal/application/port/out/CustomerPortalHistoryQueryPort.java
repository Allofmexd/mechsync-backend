package com.mechsync.modules.customerportal.application.port.out;

import com.mechsync.modules.customerportal.domain.model.CustomerPortalHistoryEvent;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalPage;

public interface CustomerPortalHistoryQueryPort {
    CustomerPortalPage<CustomerPortalHistoryEvent> findHistory(
            Long customerId, int page, int size, Long vehicleId);
}
