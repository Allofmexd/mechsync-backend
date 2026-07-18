package com.mechsync.modules.services.application.usecase;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mechsync.modules.services.application.dto.ServiceCatalogPage;
import com.mechsync.modules.services.application.port.out.ServiceCatalogRepositoryPort;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServiceCatalogServiceTest {

    @Mock
    private ServiceCatalogRepositoryPort repository;

    private ServiceCatalogService service;

    @BeforeEach
    void setUp() {
        service = new ServiceCatalogService(repository);
    }

    @Test
    void listsServicesAndNormalizesSearch() {
        ServiceCatalogPage expected = new ServiceCatalogPage(List.of(), 1, 10, 0, 0);
        when(repository.findAll(1, 10, "aceite")).thenReturn(expected);

        ServiceCatalogPage result = service.list(1, 10, "  aceite  ");

        assertSame(expected, result);
        verify(repository).findAll(1, 10, "aceite");
    }

    @Test
    void convertsBlankSearchToNull() {
        ServiceCatalogPage expected = new ServiceCatalogPage(List.of(), 0, 20, 0, 0);
        when(repository.findAll(0, 20, null)).thenReturn(expected);

        assertSame(expected, service.list(0, 20, "   "));
    }
}
