package com.mechsync.modules.parts.application.usecase;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mechsync.modules.parts.application.dto.PartCatalogPage;
import com.mechsync.modules.parts.application.port.out.PartCatalogRepositoryPort;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PartCatalogServiceTest {

    @Mock
    private PartCatalogRepositoryPort repository;

    private PartCatalogService service;

    @BeforeEach
    void setUp() {
        service = new PartCatalogService(repository);
    }

    @Test
    void listsPartsAndNormalizesSearch() {
        PartCatalogPage expected = new PartCatalogPage(List.of(), 1, 10, 0, 0);
        when(repository.findAll(1, 10, "filtro")).thenReturn(expected);

        PartCatalogPage result = service.list(1, 10, "  filtro  ");

        assertSame(expected, result);
        verify(repository).findAll(1, 10, "filtro");
    }

    @Test
    void convertsBlankSearchToNull() {
        PartCatalogPage expected = new PartCatalogPage(List.of(), 0, 20, 0, 0);
        when(repository.findAll(0, 20, null)).thenReturn(expected);

        assertSame(expected, service.list(0, 20, "   "));
    }
}
