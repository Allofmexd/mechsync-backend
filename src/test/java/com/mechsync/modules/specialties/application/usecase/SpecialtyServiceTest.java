package com.mechsync.modules.specialties.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.mechsync.modules.specialties.application.port.out.SpecialtyRepositoryPort;
import com.mechsync.modules.specialties.domain.model.Specialty;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpecialtyServiceTest {

    @Mock
    private SpecialtyRepositoryPort repository;

    @InjectMocks
    private SpecialtyService service;

    @Test
    void returnsRepositoryCatalogInNameOrder() {
        List<Specialty> catalog = List.of(
                new Specialty(2L, "DIAGNOSTICO_ELECTRONICO", "Diagnóstico"),
                new Specialty(1L, "TRANSMISIONES_AUTOMATICAS", "Transmisiones"));
        when(repository.findAllByName()).thenReturn(catalog);

        assertEquals(catalog, service.list());
    }

    @Test
    void emptyCatalogIsValid() {
        when(repository.findAllByName()).thenReturn(List.of());

        assertEquals(List.of(), service.list());
    }
}
