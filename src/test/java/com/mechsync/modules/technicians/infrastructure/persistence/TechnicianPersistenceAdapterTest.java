package com.mechsync.modules.technicians.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.mechsync.modules.technicians.domain.model.Technician;
import com.mechsync.modules.technicians.infrastructure.repository.TechnicianDetailsProjection;
import com.mechsync.modules.technicians.infrastructure.repository.TechnicianJpaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TechnicianPersistenceAdapterTest {

    @Mock
    private TechnicianJpaRepository repository;

    @Mock
    private TechnicianDetailsProjection projection;

    private TechnicianPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new TechnicianPersistenceAdapter(repository);
    }

    @Test
    void listsJoinedTechnicianDetails() {
        prepareProjection();
        when(repository.findAllDetails()).thenReturn(List.of(projection));

        List<Technician> result = adapter.findAll();

        assertEquals(1, result.size());
        assertEquals("tecnico.qa@mechsync.local", result.get(0).email());
        assertEquals("TRANSMISIONES_AUTOMATICAS", result.get(0).specialtyCode());
    }

    @Test
    void findsTechnicianByIdWithUserAndSpecialty() {
        prepareProjection();
        when(repository.findDetailsById(1L)).thenReturn(Optional.of(projection));

        Optional<Technician> result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(7L, result.orElseThrow().userId());
    }

    @Test
    void findsTechnicianByAuthenticatedUserId() {
        prepareProjection();
        when(repository.findDetailsByUserId(7L)).thenReturn(Optional.of(projection));

        Optional<Technician> result = adapter.findByUserId(7L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.orElseThrow().id());
    }

    @Test
    void convertsValidationCountsToFlags() {
        when(repository.countUsersById(7L)).thenReturn(1L);
        when(repository.countUsersById(99L)).thenReturn(0L);
        when(repository.countUserRoles(7L, "TECNICO")).thenReturn(1L);
        when(repository.countUserRoles(2L, "TECNICO")).thenReturn(0L);
        when(repository.countSpecialtiesById(1L)).thenReturn(1L);
        when(repository.countSpecialtiesById(99L)).thenReturn(0L);

        assertTrue(adapter.userExists(7L));
        assertFalse(adapter.userExists(99L));
        assertTrue(adapter.userHasRole(7L, "TECNICO"));
        assertFalse(adapter.userHasRole(2L, "TECNICO"));
        assertTrue(adapter.specialtyExists(1L));
        assertFalse(adapter.specialtyExists(99L));
    }

    @Test
    void savesEntityAndReloadsJoinedProjection() {
        prepareProjection();
        TechnicianJpaEntity savedEntity = new TechnicianJpaEntity(
                1L, 7L, 1L, LocalDate.of(2026, 1, 15), null, null);
        when(repository.saveAndFlush(any())).thenReturn(savedEntity);
        when(repository.findDetailsById(1L)).thenReturn(Optional.of(projection));

        Technician result = adapter.save(new Technician(
                null, 7L, null, null, null, null, 1L, null,
                LocalDate.of(2026, 1, 15), null, null));

        assertEquals(1L, result.id());
        assertEquals(7L, result.userId());
    }

    private void prepareProjection() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 7, 18, 12, 0);
        when(projection.getId()).thenReturn(1L);
        when(projection.getUserId()).thenReturn(7L);
        when(projection.getFirstName()).thenReturn("Tecnico");
        when(projection.getLastName()).thenReturn("QA");
        when(projection.getEmail()).thenReturn("tecnico.qa@mechsync.local");
        when(projection.getPhone()).thenReturn("9610000000");
        when(projection.getSpecialtyId()).thenReturn(1L);
        when(projection.getSpecialtyCode()).thenReturn("TRANSMISIONES_AUTOMATICAS");
        when(projection.getHireDate()).thenReturn(LocalDate.of(2026, 1, 15));
        when(projection.getCreatedAt()).thenReturn(timestamp);
        when(projection.getUpdatedAt()).thenReturn(null);
    }
}
