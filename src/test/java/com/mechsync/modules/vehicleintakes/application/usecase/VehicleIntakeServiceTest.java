package com.mechsync.modules.vehicleintakes.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mechsync.modules.vehicleintakes.application.dto.*;
import com.mechsync.modules.vehicleintakes.application.port.out.VehicleIntakeRepositoryPort;
import com.mechsync.modules.vehicleintakes.domain.exception.*;
import com.mechsync.modules.vehicleintakes.domain.model.VehicleIntake;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleIntakeServiceTest {
    @Mock VehicleIntakeRepositoryPort repository;
    VehicleIntakeService service;
    @BeforeEach void setUp() { service = new VehicleIntakeService(repository); }

    @Test void createsIntakeWithValidReferences() {
        when(repository.vehicleExists(2L)).thenReturn(true);
        when(repository.technicianExists(3L)).thenReturn(true);
        when(repository.intakeStatusExists(7L)).thenReturn(true);
        when(repository.save(any())).thenAnswer(i -> { VehicleIntake v=i.getArgument(0); return new VehicleIntake(
                1L,v.vehicleId(),v.technicianId(),v.intakeDate(),v.intakeMileage(),v.reportedProblem(),
                v.initialObservations(),v.statusId(),LocalDateTime.now(),null); });
        VehicleIntake result=service.create(new CreateVehicleIntakeCommand(2L,3L,null,100,
                " Problem "," Notes ",7L));
        assertEquals(1L,result.id()); assertEquals("Problem",result.reportedProblem());
    }
    @Test void rejectsMissingVehicle() {
        when(repository.vehicleExists(99L)).thenReturn(false);
        assertThrows(VehicleIntakeVehicleNotFoundException.class, () -> service.create(command(99L,7L)));
        verify(repository,never()).save(any());
    }
    @Test void rejectsMissingTechnician() {
        when(repository.vehicleExists(2L)).thenReturn(true); when(repository.technicianExists(3L)).thenReturn(false);
        assertThrows(VehicleIntakeTechnicianNotFoundException.class, () -> service.create(
                new CreateVehicleIntakeCommand(2L,3L,null,100,"Problem",null,7L)));
    }
    @Test void rejectsStatusOutsideIntakeContext() {
        when(repository.vehicleExists(2L)).thenReturn(true); when(repository.intakeStatusExists(1L)).thenReturn(false);
        assertThrows(VehicleIntakeStatusNotFoundException.class, () -> service.create(command(2L,1L)));
    }
    @Test void missingIntakeThrowsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(VehicleIntakeNotFoundException.class, () -> service.getById(99L));
    }
    @Test void updatePreservesVehicle() {
        when(repository.findById(1L)).thenReturn(Optional.of(intake()));
        when(repository.vehicleExists(2L)).thenReturn(true); when(repository.intakeStatusExists(8L)).thenReturn(true);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        VehicleIntake result=service.update(new UpdateVehicleIntakeCommand(1L,null,null,200,
                "Updated",null,8L));
        assertEquals(2L,result.vehicleId()); assertEquals(8L,result.statusId());
    }
    @Test void deletesWithoutWorkOrders() {
        when(repository.findById(1L)).thenReturn(Optional.of(intake()));
        service.delete(1L); verify(repository).deleteById(1L);
    }
    @Test void refusesDeleteWithWorkOrders() {
        when(repository.findById(1L)).thenReturn(Optional.of(intake())); when(repository.hasWorkOrders(1L)).thenReturn(true);
        assertThrows(VehicleIntakeInUseException.class, () -> service.delete(1L));
        verify(repository,never()).deleteById(1L);
    }
    private CreateVehicleIntakeCommand command(Long vehicle,Long status) {
        return new CreateVehicleIntakeCommand(vehicle,null,null,100,"Problem",null,status);
    }
    private VehicleIntake intake() { return new VehicleIntake(1L,2L,null,LocalDateTime.of(2026,7,12,10,0),
            100,"Problem",null,7L,LocalDateTime.of(2026,7,12,10,0),null); }
}
