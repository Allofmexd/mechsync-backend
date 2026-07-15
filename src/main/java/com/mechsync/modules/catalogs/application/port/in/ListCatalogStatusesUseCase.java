package com.mechsync.modules.catalogs.application.port.in;

import com.mechsync.modules.catalogs.domain.model.CatalogStatus;
import java.util.List;

public interface ListCatalogStatusesUseCase {

    List<CatalogStatus> listByContext(String context);
}
