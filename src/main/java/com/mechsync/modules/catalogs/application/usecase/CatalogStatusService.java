package com.mechsync.modules.catalogs.application.usecase;

import com.mechsync.modules.catalogs.application.port.in.ListCatalogStatusesUseCase;
import com.mechsync.modules.catalogs.application.port.out.CatalogStatusRepositoryPort;
import com.mechsync.modules.catalogs.domain.model.CatalogStatus;
import com.mechsync.modules.catalogs.domain.model.StatusContext;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CatalogStatusService implements ListCatalogStatusesUseCase {

    private final CatalogStatusRepositoryPort repository;

    public CatalogStatusService(CatalogStatusRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public List<CatalogStatus> listByContext(String context) {
        return repository.findByContext(StatusContext.from(context));
    }
}
