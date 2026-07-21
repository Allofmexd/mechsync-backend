package com.mechsync.modules.customerportal.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mechsync.modules.customerportal.application.port.out.CustomerPortalQueryPort;
import com.mechsync.modules.customerportal.domain.exception.CustomerPortalVehicleNotFoundException;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalProfile;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalVehicleDetail;
import com.mechsync.modules.customerportal.domain.model.CustomerPortalVehiclePage;
import com.mechsync.modules.customers.application.port.in.ResolveAuthenticatedCustomerUseCase;
import com.mechsync.modules.customers.domain.exception.CustomerIntegrityException;
import com.mechsync.modules.vehicles.application.dto.VehiclePage;
import com.mechsync.modules.vehicles.domain.model.Vehicle;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerPortalServiceTest {

    @Mock
    private ResolveAuthenticatedCustomerUseCase resolver;

    @Mock
    private CustomerPortalQueryPort queryPort;

    private CustomerPortalService service;

    @BeforeEach
    void setUp() {
        service = new CustomerPortalService(resolver, queryPort);
        when(resolver.resolveId()).thenReturn(3L);
    }

    @Test
    void returnsSafeProfileForResolvedCustomer() {
        CustomerPortalProfile profile = profile();
        when(queryPort.findProfileByCustomerId(3L)).thenReturn(Optional.of(profile));

        assertEquals(profile, service.getProfile());
        verify(queryPort).findProfileByCustomerId(3L);
    }

    @Test
    void missingJoinedProfileIsControlledIntegrityFailure() {
        when(queryPort.findProfileByCustomerId(3L)).thenReturn(Optional.empty());

        assertThrows(CustomerIntegrityException.class, service::getProfile);
    }

    @Test
    void listsOnlyResolvedCustomerPageAndMasksVin() {
        when(queryPort.findVehiclesByCustomerId(3L, 1, 5)).thenReturn(new VehiclePage(
                List.of(vehicle(8L, 3L, "1HGCM82633A001234")), 1, 5, 6, 2));

        CustomerPortalVehiclePage result = service.listVehicles(1, 5);

        assertEquals(6, result.totalElements());
        assertEquals("*************1234", result.content().get(0).maskedVin());
        assertEquals("Honda Accord 2022", result.content().get(0).description());
        verify(queryPort).findVehiclesByCustomerId(3L, 1, 5);
    }

    @Test
    void listPreservesNullVin() {
        when(queryPort.findVehiclesByCustomerId(3L, 0, 20)).thenReturn(new VehiclePage(
                List.of(vehicle(8L, 3L, null)), 0, 20, 1, 1));

        assertNull(service.listVehicles(0, 20).content().get(0).maskedVin());
    }

    @Test
    void returnsFullVinForOwnedVehicleDetail() {
        Vehicle vehicle = vehicle(8L, 3L, "1HGCM82633A001234");
        when(queryPort.findVehicleByIdAndCustomerId(8L, 3L)).thenReturn(Optional.of(vehicle));

        CustomerPortalVehicleDetail result = service.getVehicle(8L);

        assertEquals("1HGCM82633A001234", result.vin());
        verify(queryPort).findVehicleByIdAndCustomerId(8L, 3L);
    }

    @Test
    void foreignAndMissingVehicleShareNotFoundBehavior() {
        when(queryPort.findVehicleByIdAndCustomerId(99L, 3L)).thenReturn(Optional.empty());

        assertThrows(CustomerPortalVehicleNotFoundException.class, () -> service.getVehicle(99L));
    }

    private CustomerPortalProfile profile() {
        return new CustomerPortalProfile(
                3L, "Ana", "Cliente", "ana@example.com", "9610000000", "Dirección");
    }

    private Vehicle vehicle(Long id, Long customerId, String vin) {
        return new Vehicle(
                id, customerId, "Honda", "Accord", 2022, "Rojo", "ABC-123", vin,
                45000, LocalDateTime.of(2026, 7, 21, 12, 0), null);
    }
}
