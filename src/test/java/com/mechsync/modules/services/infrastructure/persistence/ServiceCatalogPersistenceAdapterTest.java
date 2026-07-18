package com.mechsync.modules.services.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mechsync.modules.services.application.dto.ServiceCatalogPage;
import com.mechsync.modules.services.infrastructure.repository.ServiceCatalogJpaRepository;
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
class ServiceCatalogPersistenceAdapterTest {

    @Mock
    private ServiceCatalogJpaRepository repository;

    private ServiceCatalogPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ServiceCatalogPersistenceAdapter(repository);
    }

    @Test
    void listsAndMapsServices() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 7, 18, 12, 0);
        ServiceCatalogJpaEntity entity = new ServiceCatalogJpaEntity(1L, "Diagnostico",
                "Revision electronica", new BigDecimal("500.00"),
                new BigDecimal("1.50"), 2L, timestamp, null);
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(entity)));

        ServiceCatalogPage result = adapter.findAll(0, 20, null);

        assertEquals(1, result.content().size());
        assertEquals("Diagnostico", result.content().get(0).name());
        assertEquals(new BigDecimal("500.00"), result.content().get(0).basePrice());
    }

    @Test
    void delegatesTextSearchToRepository() {
        when(repository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                org.mockito.ArgumentMatchers.eq("aceite"),
                org.mockito.ArgumentMatchers.eq("aceite"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        adapter.findAll(0, 20, "aceite");

        verify(repository).findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                org.mockito.ArgumentMatchers.eq("aceite"),
                org.mockito.ArgumentMatchers.eq("aceite"), any(Pageable.class));
    }
}
