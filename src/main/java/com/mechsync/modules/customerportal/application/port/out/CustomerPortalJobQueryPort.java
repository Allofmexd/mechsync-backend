package com.mechsync.modules.customerportal.application.port.out;

import com.mechsync.modules.customerportal.domain.model.CustomerPortalJobDetail;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalJobSummary;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalPage;
import java.util.Optional;

public interface CustomerPortalJobQueryPort {
    CustomerPortalPage<CustomerPortalJobSummary> findJobs(
            Long customerId, int page, int size, Long vehicleId, Long workOrderId);
    Optional<CustomerPortalJobDetail> findJob(Long customerId, Long jobId);
}
