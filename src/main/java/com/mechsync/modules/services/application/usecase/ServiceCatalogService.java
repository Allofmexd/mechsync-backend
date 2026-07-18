package com.mechsync.modules.services.application.usecase;

import com.mechsync.modules.services.application.dto.ServiceCatalogPage;
import com.mechsync.modules.services.application.port.in.ListServicesUseCase;
import com.mechsync.modules.services.application.port.out.ServiceCatalogRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ServiceCatalogService implements ListServicesUseCase {

    private final ServiceCatalogRepositoryPort repository;

    public ServiceCatalogService(ServiceCatalogRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public ServiceCatalogPage list(int page, int size, String search) {
        return repository.findAll(page, size, normalizeSearch(search));
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return search.trim();
    }
}
