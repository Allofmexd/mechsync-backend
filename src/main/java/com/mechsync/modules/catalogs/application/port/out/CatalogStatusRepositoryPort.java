package com.mechsync.modules.catalogs.application.port.out;

import com.mechsync.modules.catalogs.domain.model.CatalogStatus;
import com.mechsync.modules.catalogs.domain.model.StatusContext;
import java.util.List;

public interface CatalogStatusRepositoryPort {

    List<CatalogStatus> findByContext(StatusContext context);
}
