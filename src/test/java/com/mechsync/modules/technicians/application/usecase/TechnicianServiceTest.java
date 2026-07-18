package com.mechsync.modules.technicians.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mechsync.modules.technicians.application.dto.CreateTechnicianCommand;
import com.mechsync.modules.technicians.application.dto.UpdateTechnicianCommand;
import com.mechsync.modules.technicians.application.port.out.TechnicianRepositoryPort;
import com.mechsync.modules.technicians.domain.exception.DuplicateTechnicianException;
import com.mechsync.modules.technicians.domain.exception.TechnicianNotFoundException;
import com.mechsync.modules.technicians.domain.exception.TechnicianSpecialtyNotFoundException;
import com.mechsync.modules.technicians.domain.exception.TechnicianUserNotFoundException;
import com.mechsync.modules.technicians.domain.exception.TechnicianUserRoleRequiredException;
import com.mechsync.modules.technicians.domain.model.Technician;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TechnicianServiceTest {

    @Mock
    private TechnicianRepositoryPort repository;

    private TechnicianService service;

    @BeforeEach
    void setUp() {
        service = new TechnicianService(repository);
    }

    @Test
    void createsProfileForUserWithTechnicianRole() {
        allowCreation(7L, 1L);
        when(repository.save(any())).thenAnswer(invocation -> {
            Technician requested = invocation.getArgument(0);
            return technician(1L, requested.userId(), requested.specialtyId(), requested.hireDate());
        });

        Technician result = service.create(new CreateTechnicianCommand(
                7L, 1L, LocalDate.of(2026, 7, 18)));

        assertEquals(1L, result.id());
        assertEquals(7L, result.userId());
        assertEquals(1L, result.specialtyId());
    }

    @Test
    void missingUserCannotCreateProfile() {
        when(repository.userExists(99L)).thenReturn(false);

        assertThrows(TechnicianUserNotFoundException.class,
                () -> service.create(new CreateTechnicianCommand(99L, 1L, null)));
        verify(repository, never()).save(any());
    }

    @Test
    void userWithoutTechnicianRoleCannotCreateProfile() {
        when(repository.userExists(2L)).thenReturn(true);
        when(repository.userHasRole(2L, "TECNICO")).thenReturn(false);

        assertThrows(TechnicianUserRoleRequiredException.class,
                () -> service.create(new CreateTechnicianCommand(2L, 1L, null)));
        verify(repository, never()).save(any());
    }

    @Test
    void missingSpecialtyCannotCreateProfile() {
        when(repository.userExists(7L)).thenReturn(true);
        when(repository.userHasRole(7L, "TECNICO")).thenReturn(true);
        when(repository.specialtyExists(99L)).thenReturn(false);

        assertThrows(TechnicianSpecialtyNotFoundException.class,
                () -> service.create(new CreateTechnicianCommand(7L, 99L, null)));
        verify(repository, never()).save(any());
    }

    @Test
    void duplicateProfileIsRejected() {
        when(repository.userExists(7L)).thenReturn(true);
        when(repository.userHasRole(7L, "TECNICO")).thenReturn(true);
        when(repository.specialtyExists(1L)).thenReturn(true);
        when(repository.existsByUserId(7L)).thenReturn(true);

        assertThrows(DuplicateTechnicianException.class,
                () -> service.create(new CreateTechnicianCommand(7L, 1L, null)));
        verify(repository, never()).save(any());
    }

    @Test
    void returnsExistingTechnician() {
        Technician technician = technician(1L, 7L, 1L, null);
        when(repository.findById(1L)).thenReturn(Optional.of(technician));

        assertEquals(technician, service.getById(1L));
    }

    @Test
    void missingTechnicianThrowsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TechnicianNotFoundException.class, () -> service.getById(99L));
    }

    @Test
    void updatesSpecialtyAndHireDateWithoutChangingUser() {
        when(repository.findById(1L)).thenReturn(Optional.of(technician(1L, 7L, 1L, null)));
        when(repository.specialtyExists(2L)).thenReturn(true);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Technician result = service.update(new UpdateTechnicianCommand(
                1L, 2L, LocalDate.of(2026, 1, 15)));

        ArgumentCaptor<Technician> captor = ArgumentCaptor.forClass(Technician.class);
        verify(repository).save(captor.capture());
        assertEquals(7L, captor.getValue().userId());
        assertEquals(2L, result.specialtyId());
        assertEquals(LocalDate.of(2026, 1, 15), result.hireDate());
        assertNotNull(result.updatedAt());
    }

    @Test
    void updateRejectsMissingSpecialty() {
        when(repository.findById(1L)).thenReturn(Optional.of(technician(1L, 7L, 1L, null)));
        when(repository.specialtyExists(99L)).thenReturn(false);

        assertThrows(TechnicianSpecialtyNotFoundException.class,
                () -> service.update(new UpdateTechnicianCommand(1L, 99L, null)));
        verify(repository, never()).save(any());
    }

    private void allowCreation(Long userId, Long specialtyId) {
        when(repository.userExists(userId)).thenReturn(true);
        when(repository.userHasRole(userId, "TECNICO")).thenReturn(true);
        when(repository.specialtyExists(specialtyId)).thenReturn(true);
        when(repository.existsByUserId(userId)).thenReturn(false);
    }

    private Technician technician(Long id, Long userId, Long specialtyId, LocalDate hireDate) {
        LocalDateTime timestamp = LocalDateTime.of(2026, 7, 18, 12, 0);
        return new Technician(
                id,
                userId,
                "Tecnico",
                "QA",
                "tecnico.qa@mechsync.local",
                "9610000000",
                specialtyId,
                "TRANSMISIONES_AUTOMATICAS",
                hireDate,
                timestamp,
                null);
    }
}
