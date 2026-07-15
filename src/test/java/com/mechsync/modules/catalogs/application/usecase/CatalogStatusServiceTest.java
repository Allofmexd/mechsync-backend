package com.mechsync.modules.catalogs.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.mechsync.modules.catalogs.application.port.out.CatalogStatusRepositoryPort;
import com.mechsync.modules.catalogs.domain.exception.InvalidStatusContextException;
import com.mechsync.modules.catalogs.domain.model.CatalogStatus;
import com.mechsync.modules.catalogs.domain.model.StatusContext;
import java.util.List;
import org.junit.jupiter.api.Test;

class CatalogStatusServiceTest {

    private final CatalogStatusRepositoryPort repository = mock(CatalogStatusRepositoryPort.class);
    private final CatalogStatusService service = new CatalogStatusService(repository);

    @Test
    void filtersStatusesByVehicleIntakesContext() {
        CatalogStatus expected = new CatalogStatus(
                7L,
                StatusContext.VEHICLE_INTAKES,
                "EN_DIAGNOSTICO",
                "Vehiculo en revision inicial y diagnostico.");
        when(repository.findByContext(StatusContext.VEHICLE_INTAKES))
                .thenReturn(List.of(expected));

        List<CatalogStatus> result = service.listByContext("VEHICLE_INTAKES");

        assertEquals(List.of(expected), result);
        verify(repository).findByContext(StatusContext.VEHICLE_INTAKES);
    }

    @Test
    void invalidContextIsRejectedBeforeQueryingPersistence() {
        assertThrows(
                InvalidStatusContextException.class,
                () -> service.listByContext("UNKNOWN_CONTEXT"));

        verifyNoInteractions(repository);
    }
}
