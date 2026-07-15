package com.mechsync.modules.catalogs.infrastructure.persistence;

import com.mechsync.modules.catalogs.application.port.out.CatalogStatusRepositoryPort;
import com.mechsync.modules.catalogs.domain.model.CatalogStatus;
import com.mechsync.modules.catalogs.domain.model.StatusContext;
import com.mechsync.modules.catalogs.infrastructure.repository.CatalogStatusJpaRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CatalogStatusPersistenceAdapter implements CatalogStatusRepositoryPort {

    private final CatalogStatusJpaRepository repository;

    public CatalogStatusPersistenceAdapter(CatalogStatusJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<CatalogStatus> findByContext(StatusContext context) {
        return repository.findAllByContextOrderByIdAsc(context.name()).stream()
                .map(entity -> new CatalogStatus(
                        entity.getId(),
                        StatusContext.valueOf(entity.getContext()),
                        entity.getCode(),
                        entity.getDescription()))
                .toList();
    }
}
