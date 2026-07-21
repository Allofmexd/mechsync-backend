package com.mechsync.modules.customerportal.application.port.out;

import com.mechsync.modules.customerportal.domain.model.CustomerPortalIntakeDetail;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalIntakeSummary;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalPage;
import java.util.Optional;

public interface CustomerPortalIntakeQueryPort {
    CustomerPortalPage<CustomerPortalIntakeSummary> findIntakes(
            Long customerId, int page, int size, Long vehicleId);
    Optional<CustomerPortalIntakeDetail> findIntake(Long customerId, Long intakeId);
    boolean ownsIntake(Long customerId, Long intakeId);
}
