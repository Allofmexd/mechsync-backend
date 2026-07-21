package com.mechsync.modules.customerportal.application.usecase;

import com.mechsync.modules.customerportal.application.port.in.CustomerPortalOperationsUseCase;
import com.mechsync.modules.customerportal.application.port.out.CustomerPortalHistoryQueryPort;
import com.mechsync.modules.customerportal.application.port.out.CustomerPortalIntakeQueryPort;
import com.mechsync.modules.customerportal.application.port.out.CustomerPortalJobQueryPort;
import com.mechsync.modules.customerportal.application.port.out.CustomerPortalQueryPort;
import com.mechsync.modules.customerportal.application.port.out.CustomerPortalWorkOrderQueryPort;
import com.mechsync.modules.customerportal.domain.exception.CustomerPortalResourceNotFoundException;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalHistoryEvent;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalIntakeDetail;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalIntakeSummary;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalJobDetail;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalJobSummary;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalPage;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalQuotation;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalWorkOrderDetail;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalWorkOrderSummary;
import com.mechsync.modules.customers.application.port.in.ResolveAuthenticatedCustomerUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerPortalOperationsService implements CustomerPortalOperationsUseCase {

    private final ResolveAuthenticatedCustomerUseCase customerResolver;
    private final CustomerPortalQueryPort profileVehiclePort;
    private final CustomerPortalIntakeQueryPort intakePort;
    private final CustomerPortalWorkOrderQueryPort workOrderPort;
    private final CustomerPortalJobQueryPort jobPort;
    private final CustomerPortalHistoryQueryPort historyPort;

    public CustomerPortalOperationsService(
            ResolveAuthenticatedCustomerUseCase customerResolver,
            CustomerPortalQueryPort profileVehiclePort,
            CustomerPortalIntakeQueryPort intakePort,
            CustomerPortalWorkOrderQueryPort workOrderPort,
            CustomerPortalJobQueryPort jobPort,
            CustomerPortalHistoryQueryPort historyPort) {
        this.customerResolver = customerResolver;
        this.profileVehiclePort = profileVehiclePort;
        this.intakePort = intakePort;
        this.workOrderPort = workOrderPort;
        this.jobPort = jobPort;
        this.historyPort = historyPort;
    }

    @Override
    public CustomerPortalPage<CustomerPortalIntakeSummary> listIntakes(
            int page, int size, Long vehicleId) {
        Long customerId = customerResolver.resolveId();
        validateVehicle(customerId, vehicleId);
        return intakePort.findIntakes(customerId, page, size, vehicleId);
    }

    @Override
    public CustomerPortalIntakeDetail getIntake(Long intakeId) {
        Long customerId = customerResolver.resolveId();
        return intakePort.findIntake(customerId, intakeId)
                .orElseThrow(() -> notFound("Ingreso no encontrado"));
    }

    @Override
    public CustomerPortalPage<CustomerPortalWorkOrderSummary> listWorkOrders(
            int page, int size, Long vehicleId, Long intakeId, boolean quotationOnly) {
        Long customerId = customerResolver.resolveId();
        validateVehicle(customerId, vehicleId);
        if (intakeId != null && !intakePort.ownsIntake(customerId, intakeId)) {
            throw notFound("Ingreso no encontrado");
        }
        return workOrderPort.findWorkOrders(
                customerId, page, size, vehicleId, intakeId, quotationOnly);
    }

    @Override
    public CustomerPortalWorkOrderDetail getWorkOrder(Long workOrderId) {
        Long customerId = customerResolver.resolveId();
        return workOrderPort.findWorkOrder(customerId, workOrderId)
                .orElseThrow(() -> notFound("Orden de trabajo no encontrada"));
    }

    @Override
    public CustomerPortalQuotation getQuotation(Long workOrderId) {
        Long customerId = customerResolver.resolveId();
        if (!workOrderPort.ownsWorkOrder(customerId, workOrderId)) {
            throw notFound("Orden de trabajo no encontrada");
        }
        return workOrderPort.findVisibleQuotation(customerId, workOrderId)
                .orElseThrow(() -> notFound("La cotización todavía no está disponible."));
    }

    @Override
    public CustomerPortalPage<CustomerPortalJobSummary> listJobs(
            int page, int size, Long vehicleId, Long workOrderId) {
        Long customerId = customerResolver.resolveId();
        validateVehicle(customerId, vehicleId);
        if (workOrderId != null && !workOrderPort.ownsWorkOrder(customerId, workOrderId)) {
            throw notFound("Orden de trabajo no encontrada");
        }
        return jobPort.findJobs(customerId, page, size, vehicleId, workOrderId);
    }

    @Override
    public CustomerPortalJobDetail getJob(Long jobId) {
        Long customerId = customerResolver.resolveId();
        return jobPort.findJob(customerId, jobId)
                .orElseThrow(() -> notFound("Trabajo no encontrado"));
    }

    @Override
    public CustomerPortalPage<CustomerPortalHistoryEvent> listHistory(
            int page, int size, Long vehicleId) {
        Long customerId = customerResolver.resolveId();
        validateVehicle(customerId, vehicleId);
        return historyPort.findHistory(customerId, page, size, vehicleId);
    }

    private void validateVehicle(Long customerId, Long vehicleId) {
        if (vehicleId != null
                && profileVehiclePort.findVehicleByIdAndCustomerId(vehicleId, customerId).isEmpty()) {
            throw notFound("Vehículo no encontrado");
        }
    }

    private CustomerPortalResourceNotFoundException notFound(String message) {
        return new CustomerPortalResourceNotFoundException(message);
    }
}
