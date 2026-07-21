package com.mechsync.modules.customerportal.application.port.out;

import com.mechsync.modules.customerportal.domain.model.CustomerPortalPage;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalQuotation;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalWorkOrderDetail;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalWorkOrderSummary;
import java.util.Optional;

public interface CustomerPortalWorkOrderQueryPort {
    CustomerPortalPage<CustomerPortalWorkOrderSummary> findWorkOrders(
            Long customerId, int page, int size, Long vehicleId, Long intakeId, boolean quotationOnly);
    Optional<CustomerPortalWorkOrderDetail> findWorkOrder(Long customerId, Long workOrderId);
    Optional<CustomerPortalQuotation> findVisibleQuotation(Long customerId, Long workOrderId);
    boolean ownsWorkOrder(Long customerId, Long workOrderId);
}
