package com.mechsync.modules.customerportal.application.port.in;

import com.mechsync.modules.customerportal.domain.model.CustomerPortalHistoryEvent;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalIntakeDetail;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalIntakeSummary;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalJobDetail;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalJobSummary;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalPage;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalQuotation;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalWorkOrderDetail;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalWorkOrderSummary;

public interface CustomerPortalOperationsUseCase {
    CustomerPortalPage<CustomerPortalIntakeSummary> listIntakes(int page, int size, Long vehicleId);
    CustomerPortalIntakeDetail getIntake(Long intakeId);
    CustomerPortalPage<CustomerPortalWorkOrderSummary> listWorkOrders(
            int page, int size, Long vehicleId, Long intakeId, boolean quotationOnly);
    CustomerPortalWorkOrderDetail getWorkOrder(Long workOrderId);
    CustomerPortalQuotation getQuotation(Long workOrderId);
    CustomerPortalPage<CustomerPortalJobSummary> listJobs(
            int page, int size, Long vehicleId, Long workOrderId);
    CustomerPortalJobDetail getJob(Long jobId);
    CustomerPortalPage<CustomerPortalHistoryEvent> listHistory(int page, int size, Long vehicleId);
}
