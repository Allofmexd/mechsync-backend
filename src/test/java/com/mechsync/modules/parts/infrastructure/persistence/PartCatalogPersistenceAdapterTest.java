package com.mechsync.modules.parts.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mechsync.modules.parts.application.dto.PartCatalogPage;
import com.mechsync.modules.parts.infrastructure.repository.PartCatalogJpaRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class PartCatalogPersistenceAdapterTest {

    @Mock
    private PartCatalogJpaRepository repository;

    private PartCatalogPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PartCatalogPersistenceAdapter(repository);
    }

    @Test
    void listsPartsWithMeasurementUnitWithoutExposingStock() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 7, 18, 12, 0);
        MeasurementUnitJpaEntity unit = new MeasurementUnitJpaEntity(1L, "PIEZA", "PZA");
        PartCatalogJpaEntity entity = new PartCatalogJpaEntity(1L, "Filtro",
                "Filtro compatible", new BigDecimal("800.00"), 25, unit, 2L,
                timestamp, null);
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));

        PartCatalogPage result = adapter.findAll(0, 20, null);

        assertEquals(1, result.content().size());
        assertEquals("Filtro", result.content().get(0).name());
        assertEquals("PIEZA", result.content().get(0).measurementUnitName());
        assertEquals("PZA", result.content().get(0).measurementUnitAbbreviation());
    }

    @Test
    void delegatesTextSearchToRepository() {
        when(repository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                org.mockito.ArgumentMatchers.eq("filtro"),
                org.mockito.ArgumentMatchers.eq("filtro"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        adapter.findAll(0, 20, "filtro");

        verify(repository).findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                org.mockito.ArgumentMatchers.eq("filtro"),
                org.mockito.ArgumentMatchers.eq("filtro"), any(Pageable.class));
    }
}
