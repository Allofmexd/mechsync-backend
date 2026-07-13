package com.mechsync.modules.vehicles.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mechsync.modules.vehicles.application.dto.CreateVehicleCommand;
import com.mechsync.modules.vehicles.application.dto.UpdateVehicleCommand;
import com.mechsync.modules.vehicles.application.port.out.VehicleRepositoryPort;
import com.mechsync.modules.vehicles.domain.exception.DuplicateVehicleException;
import com.mechsync.modules.vehicles.domain.exception.InvalidVehicleYearException;
import com.mechsync.modules.vehicles.domain.exception.VehicleCustomerNotFoundException;
import com.mechsync.modules.vehicles.domain.exception.VehicleInUseException;
import com.mechsync.modules.vehicles.domain.exception.VehicleNotFoundException;
import com.mechsync.modules.vehicles.domain.model.Vehicle;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock private VehicleRepositoryPort repository;
    private VehicleService service;

    @BeforeEach
    void setUp() {
        service = new VehicleService(repository);
    }

    @Test
    void createsVehicleForExistingCustomer() {
        when(repository.customerExists(1L)).thenReturn(true);
        when(repository.existsByLicensePlate("ABC123")).thenReturn(false);
        when(repository.existsByVin("VIN123")).thenReturn(false);
        when(repository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            Vehicle vehicle = invocation.getArgument(0);
            return new Vehicle(10L, vehicle.customerId(), vehicle.brand(), vehicle.model(), vehicle.year(),
                    vehicle.color(), vehicle.licensePlate(), vehicle.vin(), vehicle.currentMileage(),
                    LocalDateTime.now(), null);
        });

        Vehicle result = service.create(new CreateVehicleCommand(
                1L, " Nissan ", " Sentra ", 2005, " Blue ", " abc123 ", " vin123 ", 120000));

        assertEquals(10L, result.id());
        assertEquals("ABC123", result.licensePlate());
        assertEquals("VIN123", result.vin());
        assertEquals("Nissan", result.brand());
    }

    @Test
    void missingCustomerIsRejected() {
        when(repository.customerExists(99L)).thenReturn(false);

        assertThrows(VehicleCustomerNotFoundException.class, () -> service.create(new CreateVehicleCommand(
                99L, "Nissan", "Sentra", 2005, null, "ABC123", null, null)));
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void duplicatePlateIsRejected() {
        when(repository.customerExists(1L)).thenReturn(true);
        when(repository.existsByLicensePlate("ABC123")).thenReturn(true);

        assertThrows(DuplicateVehicleException.class, () -> service.create(new CreateVehicleCommand(
                1L, "Nissan", "Sentra", 2005, null, "ABC123", null, null)));
    }

    @Test
    void futureYearBeyondNextYearIsRejected() {
        int invalidYear = Year.now().getValue() + 2;

        assertThrows(InvalidVehicleYearException.class, () -> service.create(new CreateVehicleCommand(
                1L, "Nissan", "Sentra", invalidYear, null, "ABC123", null, null)));
    }

    @Test
    void missingVehicleThrowsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(VehicleNotFoundException.class, () -> service.getById(99L));
    }

    @Test
    void updatesFieldsWithoutChangingCustomer() {
        when(repository.findById(1L)).thenReturn(Optional.of(vehicle()));
        when(repository.existsByLicensePlateExcludingId("XYZ999", 1L)).thenReturn(false);
        when(repository.existsByVinExcludingId("NEWVIN", 1L)).thenReturn(false);
        when(repository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));

        Vehicle result = service.update(new UpdateVehicleCommand(
                1L, "Toyota", "Corolla", 2010, "White", "xyz999", "newvin", 90000));

        assertEquals(5L, result.customerId());
        assertEquals("XYZ999", result.licensePlate());
        assertEquals("NEWVIN", result.vin());
    }

    @Test
    void deletesVehicleWithoutIntakes() {
        when(repository.findById(1L)).thenReturn(Optional.of(vehicle()));
        when(repository.hasIntakes(1L)).thenReturn(false);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void refusesDeletionWhenVehicleHasIntakes() {
        when(repository.findById(1L)).thenReturn(Optional.of(vehicle()));
        when(repository.hasIntakes(1L)).thenReturn(true);

        assertThrows(VehicleInUseException.class, () -> service.delete(1L));
        verify(repository, never()).deleteById(1L);
    }

    private Vehicle vehicle() {
        return new Vehicle(1L, 5L, "Nissan", "Sentra", 2005, "Blue", "ABC123", "VIN123",
                120000, LocalDateTime.of(2026, 1, 1, 12, 0), null);
    }
}
