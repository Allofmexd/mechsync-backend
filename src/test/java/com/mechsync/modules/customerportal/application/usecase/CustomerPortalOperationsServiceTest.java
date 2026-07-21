package com.mechsync.modules.customerportal.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.mechsync.modules.vehicles.domain.model.Vehicle;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomerPortalOperationsServiceTest {

    private ResolveAuthenticatedCustomerUseCase resolver;
    private CustomerPortalQueryPort vehicles;
    private CustomerPortalIntakeQueryPort intakes;
    private CustomerPortalWorkOrderQueryPort workOrders;
    private CustomerPortalJobQueryPort jobs;
    private CustomerPortalHistoryQueryPort history;
    private CustomerPortalOperationsService service;

    @BeforeEach
    void setUp() {
        resolver = mock(ResolveAuthenticatedCustomerUseCase.class);
        vehicles = mock(CustomerPortalQueryPort.class);
        intakes = mock(CustomerPortalIntakeQueryPort.class);
        workOrders = mock(CustomerPortalWorkOrderQueryPort.class);
        jobs = mock(CustomerPortalJobQueryPort.class);
        history = mock(CustomerPortalHistoryQueryPort.class);
        service = new CustomerPortalOperationsService(
                resolver, vehicles, intakes, workOrders, jobs, history);
        when(resolver.resolveId()).thenReturn(3L);
    }

    @Test
    void intakeListUsesResolvedCustomerAndOwnedVehicleFilter() {
        CustomerPortalPage<CustomerPortalIntakeSummary> page = emptyPage();
        when(vehicles.findVehicleByIdAndCustomerId(8L, 3L)).thenReturn(Optional.of(vehicle()));
        when(intakes.findIntakes(3L, 1, 5, 8L)).thenReturn(page);

        assertEquals(page, service.listIntakes(1, 5, 8L));
        verify(intakes).findIntakes(3L, 1, 5, 8L);
    }

    @Test
    void foreignVehicleFilterIsNotFoundBeforeQueryingResources() {
        when(vehicles.findVehicleByIdAndCustomerId(9L, 3L)).thenReturn(Optional.empty());

        assertThrows(CustomerPortalResourceNotFoundException.class,
                () -> service.listIntakes(0, 20, 9L));
        verify(intakes, never()).findIntakes(3L, 0, 20, 9L);
    }

    @Test
    void intakeDetailUsesCustomerScopedPort() {
        CustomerPortalIntakeDetail detail = mock(CustomerPortalIntakeDetail.class);
        when(intakes.findIntake(3L, 12L)).thenReturn(Optional.of(detail));

        assertEquals(detail, service.getIntake(12L));
        assertThrows(CustomerPortalResourceNotFoundException.class, () -> service.getIntake(99L));
    }

    @Test
    void workOrderListRejectsForeignIntakeFilter() {
        when(intakes.ownsIntake(3L, 12L)).thenReturn(false);

        assertThrows(CustomerPortalResourceNotFoundException.class,
                () -> service.listWorkOrders(0, 20, null, 12L, false));
        verify(workOrders, never()).findWorkOrders(3L, 0, 20, null, 12L, false);
    }

    @Test
    void workOrderDetailUsesCustomerScopedPort() {
        CustomerPortalWorkOrderDetail detail = mock(CustomerPortalWorkOrderDetail.class);
        when(workOrders.findWorkOrder(3L, 20L)).thenReturn(Optional.of(detail));

        assertEquals(detail, service.getWorkOrder(20L));
        assertThrows(CustomerPortalResourceNotFoundException.class, () -> service.getWorkOrder(21L));
    }

    @Test
    void quotationRequiresOwnedWorkOrderAndVisibleRevision() {
        CustomerPortalQuotation quotation = quotation();
        when(workOrders.ownsWorkOrder(3L, 20L)).thenReturn(true);
        when(workOrders.findVisibleQuotation(3L, 20L)).thenReturn(Optional.of(quotation));

        assertEquals(quotation, service.getQuotation(20L));

        when(workOrders.ownsWorkOrder(3L, 21L)).thenReturn(true);
        assertThrows(CustomerPortalResourceNotFoundException.class,
                () -> service.getQuotation(21L));
        when(workOrders.ownsWorkOrder(3L, 99L)).thenReturn(false);
        assertThrows(CustomerPortalResourceNotFoundException.class,
                () -> service.getQuotation(99L));
    }

    @Test
    void jobListRejectsForeignWorkOrderFilter() {
        when(workOrders.ownsWorkOrder(3L, 20L)).thenReturn(false);

        assertThrows(CustomerPortalResourceNotFoundException.class,
                () -> service.listJobs(0, 20, null, 20L));
        verify(jobs, never()).findJobs(3L, 0, 20, null, 20L);
    }

    @Test
    void jobDetailUsesCustomerScopedPort() {
        CustomerPortalJobDetail detail = mock(CustomerPortalJobDetail.class);
        when(jobs.findJob(3L, 30L)).thenReturn(Optional.of(detail));

        assertEquals(detail, service.getJob(30L));
        assertThrows(CustomerPortalResourceNotFoundException.class, () -> service.getJob(31L));
    }

    @Test
    void historyUsesResolvedCustomerAndDoesNotMergeListsInMemory() {
        CustomerPortalPage<CustomerPortalHistoryEvent> page = emptyPage();
        when(history.findHistory(3L, 2, 10, null)).thenReturn(page);

        assertEquals(page, service.listHistory(2, 10, null));
        verify(history).findHistory(3L, 2, 10, null);
    }

    @SuppressWarnings("unchecked")
    private <T> CustomerPortalPage<T> emptyPage() {
        return new CustomerPortalPage<>(List.of(), 0, 20, 0, 0);
    }

    private Vehicle vehicle() {
        return new Vehicle(8L, 3L, "Honda", "Accord", 2022, "Rojo", "ABC", null,
                100, null, null);
    }

    private CustomerPortalQuotation quotation() {
        return new CustomerPortalQuotation(
                20L, 40L, 1, "Cotización autorizada", "MXN",
                new BigDecimal("100.0000"), true, new BigDecimal("0.160000"),
                new BigDecimal("16.0000"), new BigDecimal("116.0000"),
                null, null, null, null, null, null, List.of(), List.of());
    }
}
