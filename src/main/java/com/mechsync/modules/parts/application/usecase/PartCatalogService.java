package com.mechsync.modules.parts.application.usecase;

import com.mechsync.modules.parts.application.dto.PartCatalogPage;
import com.mechsync.modules.parts.application.port.in.ListPartsUseCase;
import com.mechsync.modules.parts.application.port.out.PartCatalogRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PartCatalogService implements ListPartsUseCase {

    private final PartCatalogRepositoryPort repository;

    public PartCatalogService(PartCatalogRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public PartCatalogPage list(int page, int size, String search) {
        return repository.findAll(page, size, normalizeSearch(search));
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return search.trim();
    }
}
